package com.lilithsthrone.game.sex;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.character.PlayerCharacter;
import com.lilithsthrone.game.character.QuestLine;
import com.lilithsthrone.game.character.attributes.ArousalLevel;
import com.lilithsthrone.game.character.body.types.PenisType;
import com.lilithsthrone.game.character.body.types.VaginaType;
import com.lilithsthrone.game.character.body.valueEnums.Capacity;
import com.lilithsthrone.game.character.body.valueEnums.CumProduction;
import com.lilithsthrone.game.character.body.valueEnums.FluidModifier;
import com.lilithsthrone.game.character.body.valueEnums.OrificePlasticity;
import com.lilithsthrone.game.character.body.valueEnums.PenisSize;
import com.lilithsthrone.game.character.effects.Fetish;
import com.lilithsthrone.game.character.effects.Perk;
import com.lilithsthrone.game.character.effects.StatusEffect;
import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.dialogue.DialogueFlagValue;
import com.lilithsthrone.game.dialogue.DialogueNodeOld;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.inventory.Rarity;
import com.lilithsthrone.game.inventory.clothing.AbstractClothing;
import com.lilithsthrone.game.inventory.clothing.ClothingType;
import com.lilithsthrone.game.inventory.clothing.CoverableArea;
import com.lilithsthrone.game.inventory.clothing.DisplacementType;
import com.lilithsthrone.game.inventory.enchanting.TFEssence;
import com.lilithsthrone.game.inventory.item.AbstractItemType;
import com.lilithsthrone.game.sex.managers.SexManagerInterface;
import com.lilithsthrone.game.sex.sexActions.SexActionCategory;
import com.lilithsthrone.game.sex.sexActions.SexActionInterface;
import com.lilithsthrone.game.sex.sexActions.SexActionType;
import com.lilithsthrone.game.sex.sexActions.SexActionUtility;
import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.BaseColour;
import com.lilithsthrone.utils.Colour;
import com.lilithsthrone.utils.SizedStack;
import com.lilithsthrone.utils.Util;

/**
 * Singleton enforced by Enum Call initialiseCombat() before using. Then call
 * startSex(), which returns the starting DialogueNode.
 *
 * @since 0.1.0
 * @version 0.1.88
 * @author Innoxia
 */
public enum Sex {
	SEX;

	/*
	 * How it works: Call initialiseSex(GameCharacter partner, boolean
	 * isPlayerDom()) first. This sets starting arousal values based on player and
	 * partner composure. (0 composure starts you at 50 arousal)
	 *
	 * Then call startSex(), which returns the starting DialogueNode After that,
	 * the Sex class automatically generates responses and descriptions based on
	 * the player and their partner.
	 *
	 * Things to account for in sex:

		Masochist/Submissive/Exhibitionist
		Femininity
		Face
		Hair
		Wings
		Tail
		Horns
		Hip size
		Skin/fur colour
		Pregnancy
		Breasts
			Race
			Rows
			Size
			Lactation
			Capacity
			Creampied
		Ass
			Race
			Size
			Wetness
			Capacity
			Virgin
			Creampied
		Vagina
			Race
			Wetness
			Capacity
			Virgin
			Clit size
			Creampied
		Penis
			Race
			Size
			Testicle size
			Capacity
			Creampied

	 * TODO
	 * finishSex() method applies after sex stat changes, increment magical tally tattoos, etc.
	 */

	// Sex variables:
	private static Map<PenetrationType, Set<OrificeType>> ongoingPenetrationMap;

	private static boolean sexStarted = false;
	private static boolean consensual;
	private static boolean subHasEqualControl;

	private static NPC partner;
	private static SexManagerInterface sexManager;
	private static String sexDescription, unequipClothingText, dyeClothingText, usingItemText;
	private static AbstractClothing clothingBeingRemoved;
	private static StringBuilder sexSB = new StringBuilder();
	private static List<SexActionInterface> availableSexActionsPlayer, miscActionsPlayer, selfActionsPlayer, sexActionsPlayer, positionActionsPlayer;
	private static SizedStack<SexActionInterface> repeatActionsPlayer;
	private static List<SexActionInterface> availableSexActionsPartner;

	private static DialogueNodeOld postSexDialogue;

	private static SexActionInterface lastUsedPlayerAction, lastUsedPartnerAction;

	private static SexPace sexPacePlayer, sexPacePartner;

	// Clothes:
	private static List<AbstractClothing> playerClothingPreSex, partnerClothingPreSex;

	private static boolean sexFinished, partnerAllowedToUseSelfActions, partnerCanRemoveOwnClothes, partnerCanRemovePlayersClothes;
	private static int numberOfPlayerOrgasms, numberOfPartnerOrgasms;

	
	private static Map<PenetrationType, Set<LubricationType>> wetPenetrationTypes;
	private static Map<OrificeType, Set<LubricationType>> wetOrificeTypes;

	private static List<CoverableArea> areasExposedPlayer, areasExposedPartner;
	private static Set<OrificeType> areasCummedInPlayer, areasCummedInPartner,
										areasStretchedPlayer, areasStretchedPartner,
										areasCurrentlyStretchingPlayer, areasCurrentlyStretchingPartner,
										areasTooLoosePlayer, areasTooLoosePartner,
										penetrationRequestsPlayer;


	private static AbstractClothing selectedClothing;

	private Sex() {
	}

	public DialogueNodeOld initialiseSex(boolean consensual, boolean subHasEqualControl, NPC partner, SexManagerInterface sexManager, DialogueNodeOld postSexDialogue){
		return initialiseSex(consensual, subHasEqualControl, partner, sexManager, postSexDialogue, "", null, null);
	}

	/**
	 * @param partner
	 * @param sexManager
	 * @param postSexDialogue
	 * @param isStormImmune
	 * @param timeTaken
	 * @return
	 */
	public DialogueNodeOld initialiseSex(
			boolean consensual,
			boolean subHasEqualControl,
			NPC partner,
			SexManagerInterface sexManager,
			DialogueNodeOld postSexDialogue,
			String sexStartDescription,
			SexPace sexPacePlayer,
			SexPace sexPacePartner) {

		SexFlags.reset();
		
		Sex.consensual = consensual;
		Sex.subHasEqualControl = subHasEqualControl;
		
		// Re-initialise all sex action variables:
		sexManager.initSexActions();
		
		Main.game.setActiveNPC(partner);
		Sex.partner = partner;
		Sex.sexManager = sexManager;
		Sex.partner.generateSexChoices();
		Sex.postSexDialogue = postSexDialogue;

		if(sexPacePlayer == null) {
			Sex.sexPacePlayer = sexManager.getStartingSexPacePlayer();
		} else {
			Sex.sexPacePlayer = sexPacePlayer;
		}
		if(sexPacePartner == null) {
			Sex.sexPacePartner = sexManager.getStartingSexPacePartner();
		} else {
			Sex.sexPacePartner = sexPacePartner;
		}

		sexStarted = false;

		partner.setLocation(Main.game.getPlayer().getLocation());

		if(isConsensual()) {
			partner.setSexConsensualCount(partner.getSexConsensualCount()+1);
		}
		if(isPlayerDom()) {
			partner.setSexAsSubCount(partner.getSexAsSubCount()+1);
		} else {
			partner.setSexAsDomCount(partner.getSexAsDomCount()+1);
		}
		
		
		ongoingPenetrationMap = new EnumMap<>(PenetrationType.class);
		
		lastUsedPlayerAction = SexActionUtility.PLAYER_NONE;
		lastUsedPartnerAction = SexActionUtility.PARTNER_NONE;

		sexFinished = false;
		partnerAllowedToUseSelfActions = true;
		partnerCanRemoveOwnClothes = sexManager.isPartnerCanRemoveOwnClothes();
		partnerCanRemovePlayersClothes = sexManager.isPartnerCanRemovePlayersClothes();
		numberOfPlayerOrgasms = 0;
		numberOfPartnerOrgasms = 0;

		availableSexActionsPlayer = new ArrayList<>();
		miscActionsPlayer = new ArrayList<>();
		selfActionsPlayer = new ArrayList<>();
		sexActionsPlayer = new ArrayList<>();
		positionActionsPlayer = new ArrayList<>();
		repeatActionsPlayer = new SizedStack<>(15);
		availableSexActionsPartner = new ArrayList<>();

		// Populate exposed areas:
		areasExposedPlayer = new ArrayList<>();
		areasExposedPartner = new ArrayList<>();

//		for (CoverableArea area : CoverableArea.values()) {
//			if (Main.game.getPlayer().isAbleToAccessCoverableArea(area, false))
//				areasExposedPlayer.add(area);
//			if (partner.isAbleToAccessCoverableArea(area, false))
//				areasExposedPartner.add(area);
//		}

		penetrationRequestsPlayer = new HashSet<>();

		areasStretchedPlayer = new HashSet<>();
		areasStretchedPartner = new HashSet<>();
		areasCurrentlyStretchingPlayer = new HashSet<>();
		areasCurrentlyStretchingPartner = new HashSet<>();
		areasTooLoosePlayer = new HashSet<>();
		areasTooLoosePartner = new HashSet<>();

		areasCummedInPlayer = new HashSet<>();
		areasCummedInPartner = new HashSet<>();

		Main.game.getPlayer().setArousal(0);
		partner.setArousal(0);
		
		// Set starting wetness values:
		wetPenetrationTypes = new EnumMap<>(PenetrationType.class);
		wetOrificeTypes = new EnumMap<>(OrificeType.class);
		
		for(PenetrationType pt : PenetrationType.values()) {
			wetPenetrationTypes.put(pt, new HashSet<>());
		}
		for(OrificeType ot : OrificeType.values()) {
			wetOrificeTypes.put(ot, new HashSet<>());
		}
		
		// Starting text:
		sexSB = new StringBuilder(sexStartDescription);

		sexSB.append(sexManager.getStartSexDescription());

		sexSB.append("<p style='text-align:center;'><b>Starting Position:</b> <b style='color:"+Colour.GENERIC_ARCANE.toWebHexString()+";'>"+sexManager.getPosition().getName()+"</b></br>"
				+"<i><b>"+sexManager.getPosition().getDescription()+"</b></i></p>");
		
		// Starting exposed:
		handleExposedDescriptions(true);

		// This method appends wet descriptions to the sexSB StringBuilder:
		calculateWetAreas(true);

		sexDescription = sexSB.toString();

		Main.game.setInSex(true);

		Main.mainController.openInventory();

		// Main.mainController.updateUI();

		// Store status of all clothes for both partners (so they can be restored afterwards):
		playerClothingPreSex = new ArrayList<>();
		partnerClothingPreSex = new ArrayList<>();

		for (AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
			playerClothingPreSex.add(c);
		}
		for (AbstractClothing c : partner.getClothingCurrentlyEquipped()) {
			partnerClothingPreSex.add(c);
		}
		
		List<AbstractClothing> clothingToStrip = new ArrayList<>();

		if(sexManager.isPlayerStartNaked()) {
			for (AbstractClothing c : Main.game.getPlayer().getClothingCurrentlyEquipped()) {
				clothingToStrip.add(c);
			}

			for (AbstractClothing c : clothingToStrip) {
				Main.game.getPlayer().unequipClothingOntoFloor(c, true, Main.game.getPlayer());
			}
		}

		clothingToStrip.clear();

		if(sexManager.isPartnerStartNaked()) {
			for (AbstractClothing c : partner.getClothingCurrentlyEquipped()) {
				clothingToStrip.add(c);
			}

			for (AbstractClothing c : clothingToStrip) {
				partner.unequipClothingOntoFloor(c, true, partner);
			}
		}

		// Populate available SexAction list:
		populatePlayerSexLists();

		return SEX_DIALOGUE;
	}

	private static void endSex() {
		Main.game.setInSex(false);
		
		// Restore clothes:
		for (AbstractClothing c : playerClothingPreSex) {
			if(!c.getClothingType().isDiscardedOnUnequip()) {
				if (!Main.game.getPlayer().getClothingCurrentlyEquipped().contains(c)) {
						if(Main.game.getPlayer().getAllClothingInInventory().contains(c)) {
							Main.game.getPlayer().equipClothingFromInventory(c, true, Main.game.getPlayer(), Main.game.getPlayer());
						} else {
							Main.game.getPlayer().equipClothingFromGround(c, true, Main.game.getPlayer());
						}
				} else {
					c.getDisplacedList().clear();
				}
			}
		}
		for (AbstractClothing c : partnerClothingPreSex) {
			if(!c.getClothingType().isDiscardedOnUnequip()) {
				if(!partner.getClothingCurrentlyEquipped().contains(c)) {
						if(partner.getAllClothingInInventory().contains(c)) {
							partner.equipClothingFromInventory(c, true, partner, partner);
						} else {
							partner.equipClothingFromGround(c, true, partner);
						}
				} else {
					c.getDisplacedList().clear();
				}
			}
		}
		
		partner.setLastTimeHadSex(Main.game.getMinutesPassed());
		partner.endSex(true);
	}

	private static String endSexDescription;

	public static void applyEndSexEffects() {
		sexSB = new StringBuilder();

		// Stretching effects for each of the player's orifices:
		if (Main.game.getPlayer().getAssRawCapacityValue() != Main.game.getPlayer().getAssStretchedCapacity() && areasStretchedPlayer.contains(OrificeType.ANUS_PLAYER)) {
			if (Main.game.getPlayer().getAssPlasticity() == OrificePlasticity.ZERO_RUBBERY){

				Main.game.getPlayer().setAssStretchedCapacity(Main.game.getPlayer().getAssRawCapacityValue());

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your asshole quickly recovers from its ordeal, and instantly returns to its original size!</b></p>");

			} else {

				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				Main.game.getPlayer().incrementAssCapacity(
						(Main.game.getPlayer().getAssStretchedCapacity()-Main.game.getPlayer().getAssRawCapacityValue())*Main.game.getPlayer().getAssPlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your " + Main.game.getPlayer().getAssPlasticity().getDescriptor() + " asshole has been stretched from its ordeal, and is currently "
						+ Capacity.getCapacityFromValue(Main.game.getPlayer().getAssStretchedCapacity()).getDescriptor() + "!");

				if(Main.game.getPlayer().getAssPlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" It will recover some of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getAssRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" It will recover all of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getAssRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (Main.game.getPlayer().getVaginaRawCapacityValue() != Main.game.getPlayer().getVaginaStretchedCapacity() && areasStretchedPlayer.contains(OrificeType.VAGINA_PLAYER)) {
			if (Main.game.getPlayer().getVaginaPlasticity() == OrificePlasticity.ZERO_RUBBERY){

				Main.game.getPlayer().setVaginaStretchedCapacity(Main.game.getPlayer().getVaginaRawCapacityValue());

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your vagina quickly recovers from its ordeal, and instantly returns to its original size!</b></p>");

			} else {
				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				Main.game.getPlayer().incrementVaginaCapacity(
						(Main.game.getPlayer().getVaginaStretchedCapacity()-Main.game.getPlayer().getVaginaRawCapacityValue())*Main.game.getPlayer().getVaginaPlasticity().getCapacityIncreaseModifier(),
						false);
				
				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your " + Main.game.getPlayer().getVaginaPlasticity().getDescriptor() + " pussy has been stretched from its ordeal, and is currently "
						+ Capacity.getCapacityFromValue(Main.game.getPlayer().getVaginaStretchedCapacity()).getDescriptor() + "!");

				if(Main.game.getPlayer().getVaginaPlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" It will recover some of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getVaginaRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" It will recover all of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getVaginaRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (Main.game.getPlayer().getNippleRawCapacityValue() != Main.game.getPlayer().getNippleStretchedCapacity() && areasStretchedPlayer.contains(OrificeType.NIPPLE_PLAYER)) {
			if (Main.game.getPlayer().getNipplePlasticity() == OrificePlasticity.ZERO_RUBBERY){

				Main.game.getPlayer().setNippleStretchedCapacity(Main.game.getPlayer().getNippleRawCapacityValue());

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your [pc.nipples+] quickly recover from their ordeal, and instantly return to their original size!</b></p>");

			} else {
				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				Main.game.getPlayer().incrementNippleCapacity(
						(Main.game.getPlayer().getNippleStretchedCapacity()-Main.game.getPlayer().getNippleRawCapacityValue())*Main.game.getPlayer().getNipplePlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your " + Main.game.getPlayer().getNipplePlasticity().getDescriptor() + " nipple-cunts have been stretched from their ordeal, and are currently "
						+ Capacity.getCapacityFromValue(Main.game.getPlayer().getNippleStretchedCapacity()).getDescriptor() + "!");

				if(Main.game.getPlayer().getNipplePlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" They will recover some of their original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getNippleRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" They will recover all of their original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getNippleRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (Main.game.getPlayer().getPenisRawCapacityValue() != Main.game.getPlayer().getPenisStretchedCapacity() && areasStretchedPlayer.contains(OrificeType.URETHRA_PLAYER)) {
			if (Main.game.getPlayer().getUrethraPlasticity() == OrificePlasticity.ZERO_RUBBERY){

				Main.game.getPlayer().setPenisStretchedCapacity(Main.game.getPlayer().getPenisRawCapacityValue());

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your urethra quickly recovers from its ordeal, and instantly returns to its original size!</b></p>");

			} else {

				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				Main.game.getPlayer().incrementPenisCapacity(
						(Main.game.getPlayer().getPenisStretchedCapacity()-Main.game.getPlayer().getPenisRawCapacityValue())*Main.game.getPlayer().getUrethraPlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>Your " + Main.game.getPlayer().getUrethraPlasticity().getDescriptor() + " urethra has been stretched from its ordeal, and is currently "
						+ Capacity.getCapacityFromValue(Main.game.getPlayer().getPenisStretchedCapacity()).getDescriptor() + "!");

				if(Main.game.getPlayer().getUrethraPlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" It will recover some of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getPenisRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" It will recover all of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(Main.game.getPlayer().getPenisRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (Main.game.getPlayer().getFaceRawCapacityValue() != Main.game.getPlayer().getFaceStretchedCapacity() && areasStretchedPlayer.contains(OrificeType.MOUTH_PLAYER)) {
				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				Main.game.getPlayer().incrementFaceCapacity(
						(Main.game.getPlayer().getFaceStretchedCapacity()-Main.game.getPlayer().getFaceRawCapacityValue())*Main.game.getPlayer().getFacePlasticity().getCapacityIncreaseModifier(),
						false);
				// Special case for throat, as you aren't stretching it out, merely getting more experienced at sucking cock:
				Main.game.getPlayer().setFaceStretchedCapacity(Main.game.getPlayer().getFaceRawCapacityValue());

				sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>From your oral experience with " + partner.getName("the") + "'s " + partner.getPenisSize().getDescriptor()
						+ " cock, you are now experienced enough to comfortably suck " + PenisSize.getPenisSizeFromInt((int)Main.game.getPlayer().getFaceRawCapacityValue()).getDescriptor() + " cocks!</b></p>");
		}

		// Stretching effects for each of the partner's orifices:
		if (partner.getAssRawCapacityValue() != partner.getAssStretchedCapacity() && areasStretchedPartner.contains(OrificeType.ANUS_PARTNER)) {
			if (partner.getAssPlasticity() == OrificePlasticity.ZERO_RUBBERY){

				partner.setAssStretchedCapacity(partner.getAssRawCapacityValue());

				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] asshole quickly recovers from its ordeal, and instantly returns to its original size!</b></p>"));

			} else {

				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				partner.incrementAssCapacity(
						(partner.getAssStretchedCapacity()-partner.getAssRawCapacityValue())*partner.getAssPlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append(UtilText.parse(partner,
								"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] " + partner.getAssElasticity().getDescriptor() + " asshole has been stretched from its ordeal, and is currently "
						+ Capacity.getCapacityFromValue(partner.getAssStretchedCapacity()).getDescriptor() + "!"));

				if(partner.getAssPlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" It will recover some of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getAssRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" It will recover all of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getAssRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (partner.getVaginaRawCapacityValue() != partner.getVaginaStretchedCapacity() && areasStretchedPartner.contains(OrificeType.VAGINA_PARTNER)) {
			if (partner.getVaginaPlasticity() == OrificePlasticity.ZERO_RUBBERY){

				partner.setVaginaStretchedCapacity(partner.getVaginaRawCapacityValue());


				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] vagina quickly recovers from its ordeal, and instantly returns to its original size!</b></p>"));

			} else {

				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				partner.incrementVaginaCapacity(
						(partner.getVaginaStretchedCapacity()-partner.getVaginaRawCapacityValue())*partner.getVaginaPlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] " + partner.getVaginaPlasticity().getDescriptor() + " pussy has been stretched from its ordeal, and is currently "
						+ Capacity.getCapacityFromValue(partner.getVaginaStretchedCapacity()).getDescriptor() + "!"));

				if(partner.getVaginaPlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" It will recover some of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getVaginaRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" It will recover all of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getVaginaRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (partner.getNippleRawCapacityValue() != partner.getNippleStretchedCapacity() && areasStretchedPartner.contains(OrificeType.NIPPLE_PARTNER)) {
			if (partner.getNipplePlasticity() == OrificePlasticity.ZERO_RUBBERY){

				partner.setNippleStretchedCapacity(partner.getNippleRawCapacityValue());

				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] [npc.nipples+] quickly recover from their ordeal, and instantly return to their original size!</b></p>"));

			} else {

				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				partner.incrementNippleCapacity(
						(partner.getNippleStretchedCapacity()-partner.getNippleRawCapacityValue())*partner.getNipplePlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] " + partner.getNipplePlasticity().getDescriptor() + " nipple-cunts have been stretched from their ordeal, and are currently "
						+ Capacity.getCapacityFromValue(partner.getNippleStretchedCapacity()).getDescriptor() + "!"));

				if(partner.getNipplePlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" They will recover some of their original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getNippleRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" They will recover all of their original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getNippleRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (partner.getPenisRawCapacityValue() != partner.getPenisStretchedCapacity() && areasStretchedPartner.contains(OrificeType.URETHRA_PARTNER)) {
			if (partner.getUrethraPlasticity() == OrificePlasticity.ZERO_RUBBERY){

				partner.setPenisStretchedCapacity(partner.getPenisRawCapacityValue());

				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] urethra quickly recovers from its ordeal, and instantly returns to its original size!</b></p>"));

			} else {

				// Increment core capacity by the Elasticity's capacityIncreaseModifier:
				partner.incrementPenisCapacity(
						(partner.getPenisStretchedCapacity()-partner.getPenisRawCapacityValue())*partner.getUrethraPlasticity().getCapacityIncreaseModifier(),
						false);

				sexSB.append(UtilText.parse(partner,
						"<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>[npc.Her] " + partner.getUrethraPlasticity().getDescriptor() + " urethra has been stretched from its ordeal, and is currently "
						+ Capacity.getCapacityFromValue(partner.getPenisStretchedCapacity()).getDescriptor() + "!"));

				if(partner.getUrethraPlasticity().getCapacityIncreaseModifier()>0) {
					sexSB.append(" It will recover some of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getPenisRawCapacityValue()).getDescriptor() + "!</b></p>");
				} else {
					sexSB.append(" It will recover all of its original size, eventually tightening back to being " + Capacity.getCapacityFromValue(partner.getPenisRawCapacityValue()).getDescriptor() + "!</b></p>");
				}
			}
		}
		
		if (partner.getFaceRawCapacityValue() != partner.getFaceStretchedCapacity() && areasStretchedPartner.contains(OrificeType.MOUTH_PARTNER)) {
			// Increment core capacity by the Elasticity's capacityIncreaseModifier:
			partner.incrementFaceCapacity(
					(partner.getFaceStretchedCapacity()-partner.getFaceRawCapacityValue())*partner.getFacePlasticity().getCapacityIncreaseModifier(),
					false);
			// Special case for throat, as you aren't stretching it out, merely getting more experienced at sucking cock:
			partner.setFaceStretchedCapacity(partner.getFaceRawCapacityValue());

			sexSB.append("<p><b style='color:" + Colour.GENERIC_ARCANE.toWebHexString() + ";'>From [npc.her] oral experience with your [pc.cockSize] cock, [npc.she] is now experienced enough to comfortably suck "
					+ PenisSize.getPenisSizeFromInt((int)Main.game.getPlayer().getFaceRawCapacityValue()).getDescriptor() + " cocks!</b></p>");
		}
		

		// Player pregnancy:
		if (!areasCummedInPlayer.isEmpty()) {
			if (areasCummedInPlayer.contains(OrificeType.VAGINA_PLAYER))
				sexSB.append(Main.game.getPlayer().rollForPregnancy(partner));
		}
		// Partner pregnancy:
		if (!areasCummedInPartner.isEmpty() && partner.isAbleToBeImpregnated()) {
			if (areasCummedInPartner.contains(OrificeType.VAGINA_PARTNER))
				sexSB.append(partner.rollForPregnancy(Main.game.getPlayer()));
		}
		
		if(numberOfPlayerOrgasms==0) {
			if(Sex.getSexPacePlayer()!=SexPace.SUB_RESISTING) {
				Main.game.getPlayer().addStatusEffect(StatusEffect.FRUSTRATED_NO_ORGASM, 240+postSexDialogue.getMinutesPassed());
				sexSB.append("<p style='text-align:center'>[style.boldArcane(After finishing sex without orgasming once, you're left feeling frustrated and horny!)]</p>");
			}
			
		} else {
			Main.game.getPlayer().removeStatusEffect(StatusEffect.FRUSTRATED_NO_ORGASM);
			if(Main.game.getPlayer().hasStatusEffect(StatusEffect.RECOVERING_AURA)) {
				sexSB.append("<p style='text-align:center'><b>Your arcane aura is still strengthened from a previous sexual encounter, so</b> [style.boldArcane(you don't receive any arcane essences!)]</p>");
				
			} else {
				if(!Main.game.getDialogueFlags().values.contains(DialogueFlagValue.essenceOrgasmDiscovered)) {
					Main.game.getDialogueFlags().values.add(DialogueFlagValue.essenceOrgasmDiscovered);
					if(!Main.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
						sexSB.append(
								UtilText.parse(partner,
								"<p>"
									+ "As you disentangle yourself from [npc.name], you suddenly become aware of a strange, shimmering pink glow that's started to materialise around your body,"
										+ " just like the one you saw in Lilaya's lab when she ran her tests on you."
									+ " Quickly realising that you're somehow able to see your own arcane aura, you watch, fascinated, as it rapidly increases in luminosity."
									+ " Just as you think that it can't get any brighter, your aura suddenly leaps back into your body, and you find yourself sharply inhaling as you feel it gaining strength."
								+ "</p>"
								+ "<p>"
									+ "Looking back over at [npc.name], you see that [npc.she] seems completely oblivious to what you've just witnessed."
									+ " You think that it would probably be best to go and ask Lilaya about what just happened..."
								+ "</p>"
								+(!Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)?Main.game.getPlayer().incrementQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY):"")));
						
					} else {
						sexSB.append(
								UtilText.parse(partner,
								"<p>"
									+ "As you disentangle yourself from [npc.name], you suddenly become aware of a strange, shimmering pink glow that's started to materialise around your body,"
										+ " just like the one you saw in Lilaya's lab when she ran her tests on you."
									+ " Quickly realising that you're somehow able to see your own arcane aura, you watch, fascinated, as it rapidly increases in luminosity."
									+ " Just as you think that it can't get any brighter, your aura suddenly leaps back into your body, and you find yourself sharply inhaling as you feel it gaining strength."
								+ "</p>"
								+ "<p>"
									+ "Looking back over at [npc.name], you see that [npc.she] seems completely oblivious to what you've just witnessed."
									+ " You suddenly remember what Lilaya told you about absorbing essences, and how it's absolutely harmless for both parties involved, and breathe a sigh of relief..."
								+ "</p>"));
					}
				}
				
				sexSB.append("<p style='text-align:center'>You feel your aura pulsating all around you as it draws strength from the sexual energy of your orgasm...</p>"
						+"<div style='text-align: center; display:block; margin:0 auto; height:48px; padding:8px 0 8px 0;'>"
						+ "<div class='item-inline"
							+ (TFEssence.ARCANE.getRarity() == Rarity.COMMON ? " common" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.UNCOMMON ? " uncommon" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.RARE ? " rare" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.EPIC ? " epic" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.LEGENDARY ? " legendary" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.JINXED ? " jinxed" : "") + "'>"
							+ TFEssence.ARCANE.getSVGString()
						+ "</div>"
						+ " <div style='display:inline-block; height:20px; vertical-align: middle;'>"
							+ "<b>[style.boldArcane(You have earned "+(Main.game.getPlayer().hasPerk(Perk.NYMPHOMANIAC)?"four arcane essences":"two arcane essence")+"!)]</b>"
						+ "</div> "
						+ "<div class='item-inline"
							+ (TFEssence.ARCANE.getRarity() == Rarity.COMMON ? " common" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.UNCOMMON ? " uncommon" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.RARE ? " rare" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.EPIC ? " epic" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.LEGENDARY ? " legendary" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.JINXED ? " jinxed" : "") + "'>"
							+ TFEssence.ARCANE.getSVGString()
						+ "</div>"
						+ "</div>");
				
				if(Main.game.getPlayer().hasPerk(Perk.NYMPHOMANIAC)) {
					Main.game.getPlayer().incrementEssenceCount(TFEssence.ARCANE, 4);
				} else {
					Main.game.getPlayer().incrementEssenceCount(TFEssence.ARCANE, 2);
				}
			}
			Main.game.getPlayer().addStatusEffect(StatusEffect.RECOVERING_AURA, 240);	
		}
		
		if(numberOfPartnerOrgasms==0) {
			if(Sex.getSexPacePartner()!=SexPace.SUB_RESISTING) {
				partner.addStatusEffect(StatusEffect.FRUSTRATED_NO_ORGASM, 240+postSexDialogue.getMinutesPassed());
				sexSB.append("<p style='text-align:center'>[style.boldArcane(After finishing sex without orgasming once, [npc.name] is left feeling frustrated and horny!)]</p>");
			}
			
		} else {
			partner.removeStatusEffect(StatusEffect.FRUSTRATED_NO_ORGASM);
			if(partner.hasStatusEffect(StatusEffect.RECOVERING_AURA)) {
				sexSB.append("<p style='text-align:center'><b>[npc.Name]'s arcane aura is still strengthened from a previous sexual encounter, so</b> [style.boldArcane(you don't receive any arcane essences!)]</p>");
				
			} else {
				
				if(!Main.game.getDialogueFlags().values.contains(DialogueFlagValue.essenceOrgasmDiscovered)) {
					Main.game.getDialogueFlags().values.add(DialogueFlagValue.essenceOrgasmDiscovered);
					if(!Main.game.getPlayer().isQuestCompleted(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)) {
						sexSB.append(
								UtilText.parse(partner,
								"<p>"
									+ "As you disentangle yourself from [npc.name], you suddenly become aware of a strange, shimmering pink glow that's started to materialise around [npc.her] body,"
										+ " just like the one you saw in Lilaya's lab when she ran her tests on you."
									+ " Quickly realising that you're somehow able to see [npc.name]'s arcane aura, you watch, fascinated, as it rapidly increases in luminosity."
									+ " Just as you think that it can't get any brighter, [npc.her] aura suddenly leaps back into [npc.her] body, but as it does so, a single shard breaks off and flies towards you."
									+ " Unable to dodge in time, you find yourself sharply inhaling as the small piece of [npc.name]'s aura shoots into your chest."
								+ "</p>"
								+ "<p>"
									+ "Alarmed at what's just happened, you look back over at [npc.name], only to see that [npc.she] seems completely oblivious to what you've just witnessed."
									+ " You think that it would probably be best to go and ask Lilaya about what just happened..."
								+ "</p>"
								+(!Main.game.getPlayer().hasQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY)?Main.game.getPlayer().incrementQuest(QuestLine.SIDE_ENCHANTMENT_DISCOVERY):"")));
						
					} else {
						sexSB.append(
								UtilText.parse(partner,
								"<p>"
									+ "As you disentangle yourself from [npc.name], you suddenly become aware of a strange, shimmering pink glow that's started to materialise around [npc.her] body,"
										+ " just like the one you saw in Lilaya's lab when she ran her tests on you."
									+ " Quickly realising that you're somehow able to see [npc.name]'s arcane aura, you watch, fascinated, as it rapidly increases in luminosity."
									+ " Just as you think that it can't get any brighter, [npc.her] aura suddenly leaps back into [npc.her] body, but as it does so, a single shard breaks off and flies towards you."
									+ " Unable to dodge in time, you find yourself sharply inhaling as the small piece of [npc.name]'s aura shoots into your chest."
								+ "</p>"
								+ "<p>"
									+ "Alarmed at what's just happened, you look back over at [npc.name], only to see that [npc.she] seems completely oblivious to what you've just witnessed."
									+ " You suddenly remember what Lilaya told you about absorbing essences, and how it's absolutely harmless for both parties involved, and breathe a sigh of relief..."
								+ "</p>"));
					}
				}
				
				sexSB.append("<p style='text-align:center'>You feel your aura drawing strength from the sexual energy of [npc.name]'s orgasm...</p>"
						+"<div style='text-align: center; display:block; margin:0 auto; height:48px; padding:8px 0 8px 0;'>"
						+ "<div class='item-inline"
							+ (TFEssence.ARCANE.getRarity() == Rarity.COMMON ? " common" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.UNCOMMON ? " uncommon" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.RARE ? " rare" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.EPIC ? " epic" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.LEGENDARY ? " legendary" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.JINXED ? " jinxed" : "") + "'>"
							+ TFEssence.ARCANE.getSVGString()
						+ "</div>"
						+ " <div style='display:inline-block; height:20px; vertical-align: middle;'>"
						+ "<b>[style.boldArcane(You have earned "+(Main.game.getPlayer().hasPerk(Perk.NYMPHOMANIAC)?"four arcane essences":"two arcane essence")+"!)]</b>"
						+ "</div> "
						+ "<div class='item-inline"
							+ (TFEssence.ARCANE.getRarity() == Rarity.COMMON ? " common" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.UNCOMMON ? " uncommon" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.RARE ? " rare" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.EPIC ? " epic" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.LEGENDARY ? " legendary" : "")
							+ (TFEssence.ARCANE.getRarity() == Rarity.JINXED ? " jinxed" : "") + "'>"
							+ TFEssence.ARCANE.getSVGString()
						+ "</div>"
						+ "</div>");
				if(Main.game.getPlayer().hasPerk(Perk.NYMPHOMANIAC))
					Main.game.getPlayer().incrementEssenceCount(TFEssence.ARCANE, 4);
				else
					Main.game.getPlayer().incrementEssenceCount(TFEssence.ARCANE, 2);
				
			}
			partner.addStatusEffect(StatusEffect.RECOVERING_AURA, 240);
		}

		endSexDescription = sexSB.toString();
	}
	
	// Text formatting:
	
	private static String formatInitialPenetration(String description) {
		return "<p style='text-align:center;'><i style='color:" + BaseColour.PINK_DEEP.toWebHexString() + ";'>"+description+"</i></p>";
	}
	
	private static String formatPenetration(String rawInput){
		return "<p style='text-align:center; color:"+BaseColour.VIOLET.toWebHexString()+";'><i>"+rawInput+"</i></p>";
	}
	
	private static String formatStopPenetration(String rawInput){
		return "<p style='text-align:center; color:"+BaseColour.PINK.toWebHexString()+";'><i>"+rawInput+"</i></p>";
	}
	
	private static String formatCoverableAreaBecomingExposed(String description) {
		return "<p style='text-align:center;'><i style='color:" + BaseColour.PURPLE_LIGHT.toWebHexString() + ";'>"+description+"</i></p>";
	}
	
	private static String formatCoverableAreaGettingWet(String description) {
		return UtilText.parse(Sex.getPartner(),
				"<p style='text-align:center;'><i style='color:" + BaseColour.LILAC_LIGHT.toWebHexString() + ";'>"+description+"</i></p>");
	}

	
	public static SexActionCategory responseCategory = null;
	
	public static final DialogueNodeOld SEX_DIALOGUE = new DialogueNodeOld("", "", true) {
		private static final long serialVersionUID = 1L;

		@Override
		public String getLabel() {
			return "Sex: "+getPosition().getName();
		}

		@Override
		public String getContent() {
			return sexDescription + (sexFinished ? endSexDescription : "");
		}

		@Override
		public String getResponseTabTitle(int index) {

			if (sexFinished
					|| lastUsedPartnerAction == SexActionUtility.PARTNER_ORGASM_MUTUAL_WAIT
					|| Main.game.getPlayer().getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()
					|| partner.getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()) {
				return null;
			}
			if(index==0) {
				return "Misc. Actions";
				
			} else if(index==1) {
				return "Self Actions";
				
			} else if(index==2) {
				return "Sex Actions";
				
			} else if(index==3) {
				return "Positioning";
				
			} else if(index==4) {
				return "Repeat Actions";
				
			} else {
				return null;
			}
		}
		
		@Override
		public Response getResponse(int responseTab, int index) {
			// Finished sex:
			if (sexFinished) {
				if (index == 1) {
					return new Response(postSexDialogue.getLabel(), postSexDialogue.getDescription(), postSexDialogue){
						@Override
						public void effects() {
							endSex();
						}
					};
				} else {
					return null;
				}
				
			// Orgasm actions:
			} else if(lastUsedPartnerAction == SexActionUtility.PARTNER_ORGASM_MUTUAL_WAIT
						|| Main.game.getPlayer().getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()
						|| partner.getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()) {
					
				if(index == 0){
					return null;

				} else if (index-1 < availableSexActionsPlayer.size() && availableSexActionsPlayer.get(index-1) != null){
					return availableSexActionsPlayer.get(index-1).toResponse();
					
				} else {
					return null;
				}
				
			// Normal sex actions:
			} else {
				
				if(responseTab==0) {
					if(index <= miscActionsPlayer.size()) {
						if(index==0) {
							if(miscActionsPlayer.size()>=15) {
								return miscActionsPlayer.get(14).toResponse();
							} else {
								return null;
							}
						} else {
							return miscActionsPlayer.get(index-1).toResponse();
						}
					}
					
				} else if(responseTab==1) {
					if(index <= selfActionsPlayer.size()) {
						if(index==0) {
							if(selfActionsPlayer.size()>=15) {
								return selfActionsPlayer.get(14).toResponse();
							} else {
								return null;
							}
						} else {
							return selfActionsPlayer.get(index-1).toResponse();
						}
					}
					
				} else if(responseTab==2) {
					if(index <= sexActionsPlayer.size()) {
						if(index==0) {
							if(sexActionsPlayer.size()>=15) {
								return sexActionsPlayer.get(14).toResponse();
							} else {
								return null;
							}
						} else {
							return sexActionsPlayer.get(index-1).toResponse();
						}
					}
					
				} else if(responseTab==3) {
					if(index <= positionActionsPlayer.size()) {
						if(index==0) {
							if(positionActionsPlayer.size()>=15) {
								return positionActionsPlayer.get(14).toResponse();
							} else {
								return null;
							}
						} else {
							return positionActionsPlayer.get(index-1).toResponse();
						}
					}
					
				} else if(responseTab==4) {
					if(index <= repeatActionsPlayer.size()) {
						if(index==0) {
							if(repeatActionsPlayer.size()>=15) {
								return repeatActionsPlayer.get(14).toResponse();
							} else {
								return null;
							}
						} else {
							return repeatActionsPlayer.get(index-1).toResponse();
						}
					}
					
				}
				
				return null;
			}
		}

		@Override
		public boolean isContinuesDialogue(){
			return sexStarted;
		}

		@Override
		public boolean isDisplaysActionTitleOnContinuesDialogue() {
			return true;
		}

		@Override
		public boolean isInventoryDisabled() {
			return false;
		}
	};
	
//	public static final DialogueNodeOld SEX_DIALOGUE_RETURN = new DialogueNodeOld("", "", true) {
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public String getLabel() {
//			return "Sex: "+getPosition().getName();
//		}
//
//		@Override
//		public String getContent() {
//			return (!sexStarted?SEX_DIALOGUE.getContent():"");
//		}
//
//		@Override
//		public Response getResponse(int responseTab, int index) {
//			return SEX_DIALOGUE.getResponse(0, index);
//		}
//
//		@Override
//		public boolean isContinuesDialogue(){
//			return SEX_DIALOGUE.isContinuesDialogue();
//		}
//
//		@Override
//		public boolean isDispalysActionTitleOnContinuesDialogue() {
//			return false;
//		}
//
//		@Override
//		public boolean isInventoryDisabled() {
//			return SEX_DIALOGUE.isInventoryDisabled();
//		}
//	};
	
	/**
	 * If you call this while not in sex, you're going to seriously f*** things up.
	 * @param sexActionPlayer
	 */
	public static void endSexTurn(SexActionInterface sexActionPlayer) {

		sexSB = new StringBuilder();

		sexSB.append("<p>" + sexActionPlayer.getDescription() + "</p>");

		sexActionPlayer.baseEffects();
		lastUsedPlayerAction = sexActionPlayer;
		if(!repeatActionsPlayer.contains(sexActionPlayer)
				&& sexActionPlayer.getActionType()!=SexActionType.PLAYER_PREPARE_PARTNER_ORGASM
				&& sexActionPlayer.getActionType()!=SexActionType.PLAYER_ORGASM
				&& sexActionPlayer.getActionType()!=SexActionType.PLAYER_ORGASM_NO_AROUSAL_RESET) {
			repeatActionsPlayer.add(sexActionPlayer);
		}
		
		applyGenericDescriptionsAndEffects(sexActionPlayer);
		
		String s = UtilText.parse(sexSB.toString());
		sexSB.setLength(0);
		sexSB.append(s);
		
		// End sex conditions:
		if (sexActionPlayer.endsSex()) {

			sexDescription = sexSB.toString();

			applyEndSexEffects();
			sexFinished = true;
			
		} else {
			// Partner action is done afterwards:
			// Update lists for the partner's action choice.
			calculateAvailableSexActionsPartner();

			SexActionInterface sexActionPartner = sexManager.getPartnerSexAction(sexActionPlayer);

			sexSB.append("</br><p>" + sexActionPartner.getDescription() + "</p>");

			sexActionPartner.baseEffects();
			lastUsedPartnerAction = sexActionPartner;

			applyGenericDescriptionsAndEffects(sexActionPartner);
			
			s = UtilText.parse(sexSB.toString());
			sexSB.setLength(0);
			sexSB.append(s);

			sexDescription = sexSB.toString();

			// End sex conditions:
			if (sexActionPartner.endsSex()) {
				applyEndSexEffects();
				sexFinished = true;
			}

			// Re-populate lists for the player's next action choice.
			populatePlayerSexLists();
		}
	}

	private static void populatePlayerSexLists() {
		// Populate available SexActions from the current SexPosition.
		availableSexActionsPlayer.clear();

		if(lastUsedPartnerAction == SexActionUtility.PARTNER_ORGASM_MUTUAL_WAIT) {
			for (SexActionInterface sexAction : sexManager.getMutualOrgasmActions()) {
				if (sexAction.isAddedToAvailableSexActions()) {
					availableSexActionsPlayer.add(sexAction);
				}
			}

		} else if (Main.game.getPlayer().getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()) { // Add orgasm actions if ready to orgasm:
			
			// If mutual orgasm threshold has been reached, use a mutual orgasm:
			boolean orgasmFound = false;

			if (ArousalLevel.getArousalLevelFromValue(partner.getArousal()).isMutualOrgasm()) {
				for (SexActionInterface sexAction : sexManager.getMutualOrgasmActions()) {
					if (sexAction.isAddedToAvailableSexActions()) {
						availableSexActionsPlayer.add(sexAction);
						orgasmFound = true;
					}
				}
			}

			// If there were no mutual orgasm options available (or mutual orgasm threshold wasn't reached), use a standard one:
			if (!orgasmFound) {
				for (SexActionInterface sexAction : sexManager.getOrgasmActionsPlayer()) {
					if (sexAction.isAddedToAvailableSexActions()) {
						availableSexActionsPlayer.add(sexAction);
					}
				}
			}

		} else if (partner.getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()) { // Add orgasm reactions if ready to orgasm:
			for (SexActionInterface sexAction : sexManager.getActionsAvailablePlayer()) {
				if (sexAction.getActionType()==SexActionType.PLAYER_PREPARE_PARTNER_ORGASM && sexAction.isAddedToAvailableSexActions()) {
					availableSexActionsPlayer.add(sexAction);
				}
			}
			
		} else {
			// Can always do nothing:
			availableSexActionsPlayer.add(SexActionUtility.PLAYER_NONE);
			availableSexActionsPlayer.add(SexActionUtility.PLAYER_CALM_DOWN);

			if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DENIAL)) {
				if(isConsensual() || isPlayerDom())
					availableSexActionsPlayer.add(SexActionUtility.DENIAL_FETISH_DENY);
			}

			// Add actions:
			for (SexActionInterface sexAction : sexManager.getActionsAvailablePlayer()) {
				if (sexAction.isAddedToAvailableSexActions()){
					availableSexActionsPlayer.add(sexAction);
				}
			}

			availableSexActionsPlayer.sort((SexActionInterface s1, SexActionInterface s2) -> {return s1.getActionType().compareTo(s2.getActionType());});

		}
		
		miscActionsPlayer.clear();
		selfActionsPlayer.clear();
		sexActionsPlayer.clear();
		positionActionsPlayer.clear();
		for(SexActionInterface action : availableSexActionsPlayer) {
			switch(action.getCategory()) {
				case MISCELLANEOUS:
					miscActionsPlayer.add(action);
					break;
				case POSITIONING:
					positionActionsPlayer.add(action);
					break;
				case SELF:
					selfActionsPlayer.add(action);
					break;
				case SEX:
					sexActionsPlayer.add(action);
					break;
			}
		}

	}

	/**
	 * For use in getPartnerSexAction() in SexManager classes.
	 */
	private static void calculateAvailableSexActionsPartner() {
		// Populate available SexActions from the current SexPosition.
		availableSexActionsPartner.clear();
		List<SexActionInterface> lowPriority = new ArrayList<>(), normalPriority = new ArrayList<>(), highPriority = new ArrayList<>(), uniqueMax  = new ArrayList<>();
		boolean standardActions = true;
		
		// Add orgasm actions if ready to orgasm:
		if (partner.getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()) {
			standardActions = false;

			// If mutual orgasm threshold has been reached, use a mutual orgasm:
			boolean orgasmFound = false;

			// If mutual orgasm threshold has been reached, use a mutual orgasm:
			if (ArousalLevel.getArousalLevelFromValue(Main.game.getPlayer().getArousal()).isMutualOrgasm()) {
				for (SexActionInterface sexAction : sexManager.getMutualOrgasmActions()) {
					if (sexAction.isAddedToAvailableSexActions()) {
						availableSexActionsPartner.add(SexActionUtility.PARTNER_ORGASM_MUTUAL_WAIT);
						orgasmFound = true;
						break;
					}
				}
			}
			
			// If there were no mutual orgasm options available (or mutual orgasm threshold wasn't reached), use a standard one:
			if(!orgasmFound) {
				if(SexFlags.playerPreparedForOrgasm) {
					for (SexActionInterface sexAction : sexManager.getOrgasmActionsPartner()) {
						if (sexAction.isAddedToAvailableSexActions()) {
							switch(sexAction.getPriority()){
								case LOW:
									lowPriority.add(sexAction);
									break;
								case NORMAL:
									normalPriority.add(sexAction);
									break;
								case HIGH:
									highPriority.add(sexAction);
									break;
								case UNIQUE_MAX:
									uniqueMax.add(sexAction);
									break;
							}
						}
					}
				} else {
					standardActions = true;
				}
			}

			if(!uniqueMax.isEmpty()) {
				availableSexActionsPartner.addAll(uniqueMax);

			} else if(!highPriority.isEmpty()) {
				availableSexActionsPartner.addAll(highPriority);

			} else if(!normalPriority.isEmpty()) {
				availableSexActionsPartner.addAll(normalPriority);

			} else if(!lowPriority.isEmpty()) {
				availableSexActionsPartner.addAll(lowPriority);

			}

			// Backup just in case for some reason no orgasms were added:
			if (!availableSexActionsPartner.isEmpty()) {
				return;
			}
			
		}
		
		if (Main.game.getPlayer().getArousal() >= ArousalLevel.FIVE_ORGASM_IMMINENT.getMaximumValue()) { // Add orgasm reactions if ready to orgasm:
			for (SexActionInterface sexAction : sexManager.getActionsAvailablePartner()) {
				if (sexAction.getActionType()==SexActionType.PARTNER_PREPARE_PLAYER_ORGASM) {
					if (sexAction.isAddedToAvailableSexActions()) {
						switch(sexAction.getPriority()){
							case LOW:
								lowPriority.add(sexAction);
								break;
							case NORMAL:
								normalPriority.add(sexAction);
								break;
							case HIGH:
								highPriority.add(sexAction);
								break;
							case UNIQUE_MAX:
								uniqueMax.add(sexAction);
								break;
						}
					}
				}
			}
			
			if(!uniqueMax.isEmpty()) {
				availableSexActionsPartner.addAll(uniqueMax);

			} else if(!highPriority.isEmpty()) {
				availableSexActionsPartner.addAll(highPriority);

			} else if(!normalPriority.isEmpty()) {
				availableSexActionsPartner.addAll(normalPriority);

			} else if(!lowPriority.isEmpty()) {
				availableSexActionsPartner.addAll(lowPriority);

			}

			// Backup just in case for some reason no orgasms were added:
			if (!availableSexActionsPartner.isEmpty()) {
				return;
			}
			
		} else if(standardActions) {

			// Add actions:
			for (SexActionInterface sexAction : sexManager.getActionsAvailablePartner()) {
				if (sexAction.isAddedToAvailableSexActions() && (partnerAllowedToUseSelfActions?true:(!sexAction.isPartnerSelfPenetration()))) {
					
					if(Main.game.isNonConEnabled()
							&& getSexPacePartner()==SexPace.SUB_RESISTING
							&& ((sexAction.getSexPacePartner()!=null && sexAction.getSexPacePartner()!=SexPace.SUB_RESISTING) || sexAction.isPartnerSelfAction())) {
						// Do not add action if the partner is resisting and this action is SUB_EAGER or SUB_NORMAL or is a self action
						
					} else if(sexAction.getSexPacePartner()!=null && getSexPacePartner()!=sexAction.getSexPacePartner()) {
						// Do not add action if action does not correspond to the partner's preferred action pace
						
					} else {
						// Add action as normal:
						switch(sexAction.getPriority()){
							case LOW:
								lowPriority.add(sexAction);
								break;
							case NORMAL:
								normalPriority.add(sexAction);
								break;
							case HIGH:
								highPriority.add(sexAction);
								break;
							case UNIQUE_MAX:
								uniqueMax.add(sexAction);
								break;
						}
					}
				}
			}

			if(!uniqueMax.isEmpty()) {
				availableSexActionsPartner.addAll(uniqueMax);

			} else if(!highPriority.isEmpty()) {
				availableSexActionsPartner.addAll(highPriority);

			} else if(!normalPriority.isEmpty()) {
				availableSexActionsPartner.addAll(normalPriority);

			} else if(!lowPriority.isEmpty()) {
				availableSexActionsPartner.addAll(lowPriority);
			}
			
			// Can do nothing if they have no other options:
			if (availableSexActionsPartner.isEmpty()) {
				availableSexActionsPartner.add(SexActionUtility.PARTNER_NONE);
			}
		}

	}

	public static List<SexActionInterface> getAvailableSexActionsPartner() {
		return availableSexActionsPartner;
	}

	/** Applies extra effects and generates extra description text. */
	private static void applyGenericDescriptionsAndEffects(SexActionInterface sexAction) {

		// Set sex pace based off this SexAction:
		if(sexAction.getSexPacePlayer()!=null) {
			setSexPacePlayer(sexAction.getSexPacePlayer());//TODO append description
		}
		if(sexAction.getSexPacePartner()!=null) {
			setSexPacePartner(sexAction.getSexPacePartner());//TODO append description
		}

		// Arousal increases, taking into account fetishes and sex effects:
		float bonusArousalIncreasePlayer = 0f, bonusArousalIncreasePartner = 0f;
		for(StatusEffect se : Main.game.getPlayer().getStatusEffects()) {
			if(se.isSexEffect()) {
				bonusArousalIncreasePlayer += se.getArousalPerTurnSelf(Main.game.getPlayer());
				bonusArousalIncreasePartner += se.getArousalPerTurnPartner(partner);
			}
		}
		for(StatusEffect se : Sex.getPartner().getStatusEffects()) {
			if(se.isSexEffect()) {
				bonusArousalIncreasePlayer += se.getArousalPerTurnPartner(Main.game.getPlayer());
				bonusArousalIncreasePartner += se.getArousalPerTurnSelf(partner);
			}
		}

		if(sexAction.getFetishesPlayer()!=null) {
			for(Fetish f : sexAction.getFetishesPlayer()) {
				if(Main.game.getPlayer().hasFetish(f)) {
					bonusArousalIncreasePlayer += 2f;
				}
			}
		}
		if(sexAction.getFetishesPartner()!=null) {
			for(Fetish f : sexAction.getFetishesPartner()) {
				if(partner.hasFetish(f)) {
					bonusArousalIncreasePartner += 2f;
				}
			}
		}
		
		if(bonusArousalIncreasePlayer>6) {
			bonusArousalIncreasePlayer = 6;
		}
		if(bonusArousalIncreasePartner>6) {
			bonusArousalIncreasePartner = 6;
		}
		
		float playerArousalIncrease = (sexAction.getArousalGainPlayer().getArousalIncreaseValue() + bonusArousalIncreasePlayer);
		if(sexPacePlayer==SexPace.SUB_RESISTING) {
			if(Main.game.getPlayer().hasFetish(Fetish.FETISH_NON_CON_SUB)) {
				playerArousalIncrease*=1.25f;
			} else {
				playerArousalIncrease*=0.5f;
			}
		}
		if(sexPacePartner==SexPace.SUB_RESISTING && Main.game.getPlayer().hasFetish(Fetish.FETISH_NON_CON_DOM)) {
			playerArousalIncrease*=1.25f;
		}
		Main.game.getPlayer().incrementArousal(playerArousalIncrease);
		
		float partnerArousalIncrease = (sexAction.getArousalGainPartner().getArousalIncreaseValue() + bonusArousalIncreasePartner);
		if(sexPacePartner==SexPace.SUB_RESISTING) {
			if(partner.hasFetish(Fetish.FETISH_NON_CON_SUB)) {
				partnerArousalIncrease*=1.25f;
			} else {
				partnerArousalIncrease*=0.5f;
			}
		}
		if(sexPacePlayer==SexPace.SUB_RESISTING && partner.hasFetish(Fetish.FETISH_NON_CON_DOM)) {
			partnerArousalIncrease*=1.25f;
		}
		partner.incrementArousal(partnerArousalIncrease);

		// Cummed in areas:

		// Add any areas that have been cummed in:
		// TODO take into account all penetrationType cum variants.
		// TODO Take into account condom being used on other penetrationTypes
		if (sexAction.getPlayerAreasCummedIn() != null && partner.getPenisCumProduction() != CumProduction.ZERO_NONE) {
			if(!partner.isWearingCondom() || (partner.isWearingCondom() && sexAction.ignorePartnerCondom())){
				for(OrificeType ot : sexAction.getPlayerAreasCummedIn()) {
					areasCummedInPlayer.add(ot);
					Main.game.getPlayer().incrementCumCount(new SexType(PenetrationType.PENIS_PARTNER, ot));
					partner.incrementCumCount(new SexType(PenetrationType.PENIS_PARTNER, ot));
					sexSB.append(Main.game.getPlayer().ingestFluid(partner, partner.getCum().getType(), ot, partner.getCum().hasFluidModifier(FluidModifier.ADDICTIVE)));
					
					if(ot==OrificeType.ANUS_PLAYER) {
						Main.game.getPlayer().addStatusEffect(StatusEffect.CREAMPIE_ANUS, 120+postSexDialogue.getMinutesPassed());
					} else if(ot==OrificeType.NIPPLE_PLAYER) {
						Main.game.getPlayer().addStatusEffect(StatusEffect.CREAMPIE_NIPPLES, 120+postSexDialogue.getMinutesPassed());
					} else if(ot==OrificeType.VAGINA_PLAYER) {
						Main.game.getPlayer().addStatusEffect(StatusEffect.CREAMPIE_VAGINA, 120+postSexDialogue.getMinutesPassed());
					} else if(ot==OrificeType.URETHRA_PLAYER) {
						Main.game.getPlayer().addStatusEffect(StatusEffect.CREAMPIE_PENIS, 120+postSexDialogue.getMinutesPassed());
					}
				}
			}
		}
		if (sexAction.getPartnerAreasCummedIn() != null && Main.game.getPlayer().getPenisCumProduction() != CumProduction.ZERO_NONE) {
			if(!Main.game.getPlayer().isWearingCondom() || (Main.game.getPlayer().isWearingCondom() && sexAction.ignorePartnerCondom())){
				for(OrificeType ot : sexAction.getPartnerAreasCummedIn()) {
					areasCummedInPartner.add(ot);
					Main.game.getPlayer().incrementCumCount(new SexType(PenetrationType.PENIS_PLAYER, ot));
					partner.incrementCumCount(new SexType(PenetrationType.PENIS_PLAYER, ot));
					sexSB.append(partner.ingestFluid(Main.game.getPlayer(), Main.game.getPlayer().getCum().getType(), ot, Main.game.getPlayer().getCum().hasFluidModifier(FluidModifier.ADDICTIVE)));
					
					if(ot==OrificeType.ANUS_PARTNER) {
						partner.addStatusEffect(StatusEffect.CREAMPIE_ANUS, 120+postSexDialogue.getMinutesPassed());
					} else if(ot==OrificeType.NIPPLE_PARTNER) {
						partner.addStatusEffect(StatusEffect.CREAMPIE_NIPPLES, 120+postSexDialogue.getMinutesPassed());
					} else if(ot==OrificeType.VAGINA_PARTNER) {
						partner.addStatusEffect(StatusEffect.CREAMPIE_VAGINA, 120+postSexDialogue.getMinutesPassed());
					} else if(ot==OrificeType.URETHRA_PARTNER) {
						partner.addStatusEffect(StatusEffect.CREAMPIE_PENIS, 120+postSexDialogue.getMinutesPassed());
					}
				}
			}
		}

		if(sexAction.getActionType()==SexActionType.PLAYER_PREPARE_PARTNER_ORGASM) {
			SexFlags.playerPreparedForOrgasm = true;
		}
		
		// Handle player orgasms:
		if(sexAction.getActionType()==SexActionType.PLAYER_ORGASM) {
			// Condom removal:
			if(Main.game.getPlayer().isWearingCondom()){
				Main.game.getPlayer().getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).setSealed(false);
				if(Main.game.getPlayer().getPenisRawCumProductionValue()>0) {
					sexSB.append(Main.game.getPlayer().addItem(AbstractItemType.generateFilledCondom(Main.game.getPlayer().getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).getColour(), Main.game.getPlayer(), Main.game.getPlayer().getCum()), false));
				}
				Main.game.getPlayer().unequipClothingIntoVoid(Main.game.getPlayer().getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()), true, Main.game.getPlayer());
				
			}
			// Apply orgasm arousal resets:
			numberOfPlayerOrgasms++;
			player().setArousal(0);
			
			// Reset appropriate flags:
			SexFlags.partnerRequestedCreampie = false;
			SexFlags.partnerRequestedPullOut = false;
			
		}
		// Handle partner orgasms:
		if(sexAction.getActionType()==SexActionType.PARTNER_ORGASM) {
			// Condom removal:
			if(partner.isWearingCondom()){
				partner.getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).setSealed(false);
				if(partner.getPenisRawCumProductionValue()>0) {
					sexSB.append(Main.game.getPlayer().addItem(AbstractItemType.generateFilledCondom(partner.getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).getColour(), partner, partner.getCum()), false));
				}
				partner.unequipClothingIntoVoid(partner.getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()), true, partner);
				
			}
			// Apply orgasm arousal resets:
			numberOfPartnerOrgasms++;
			partner.setArousal(0);

			// Reset appropriate flags:
			SexFlags.playerRequestedCreampie = false;
			SexFlags.playerRequestedPullOut = false;
			SexFlags.playerPreparedForOrgasm = false;
		}
		// Handle mutual orgasms:
		if(sexAction.getActionType()==SexActionType.MUTUAL_ORGASM) {
			// Condom removal:
			if(Main.game.getPlayer().isWearingCondom()){
				Main.game.getPlayer().getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).setSealed(false);
				if(Main.game.getPlayer().getPenisRawCumProductionValue()>0) {
					sexSB.append(Main.game.getPlayer().addItem(AbstractItemType.generateFilledCondom(Main.game.getPlayer().getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).getColour(), Main.game.getPlayer(), Main.game.getPlayer().getCum()), false));
				}
				Main.game.getPlayer().unequipClothingIntoVoid(Main.game.getPlayer().getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()), true, Main.game.getPlayer());
				
			}
			if(partner.isWearingCondom()){
				partner.getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).setSealed(false);
				if(partner.getPenisRawCumProductionValue()>0) {
					sexSB.append(Main.game.getPlayer().addItem(AbstractItemType.generateFilledCondom(partner.getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()).getColour(), partner, partner.getCum()), false));
				}
				partner.unequipClothingIntoVoid(partner.getClothingInSlot(ClothingType.PENIS_CONDOM.getSlot()), true, partner);
				
			}
			// Apply orgasm arousal resets:
			numberOfPlayerOrgasms++;
			player().setArousal(0);
			// Apply orgasm arousal resets:
			numberOfPartnerOrgasms++;
			partner.setArousal(0);

			// Reset appropriate flags:
			SexFlags.partnerRequestedCreampie = false;
			SexFlags.partnerRequestedPullOut = false;
			SexFlags.playerRequestedCreampie = false;
			SexFlags.playerRequestedPullOut = false;
		}

		// Handle if parts have just become exposed:
		if (sexAction == SexActionUtility.CLOTHING_REMOVAL) {
			handleExposedDescriptions(false);
		}
		
		// Only apply penetration effects if this action isn't an orgasm, and it isn't the end of sex. (Otherwise, ongoing descriptions get appended after the main description, which usually don't make sense.) TODO
		if (!sexManager.getOrgasmActionsPlayer().contains(sexAction)
				&& !sexManager.getOrgasmActionsPartner().contains(sexAction)
				&& !sexManager.getMutualOrgasmActions().contains(sexAction)
				&& sexAction.getActionType() != SexActionType.PARTNER_POSITIONING
				&& sexAction.getActionType() != SexActionType.PLAYER_POSITIONING
				&& !sexAction.endsSex()) {
			for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
				for(OrificeType t : entry.getValue())
					applyPenetrationEffects(entry.getKey(), t);
			}
		}
		
		calculateWetAreas(false);
	}
	
	private static void handleExposedDescriptions(boolean atStartOfSex) {
		// Player:
		if (!areasExposedPlayer.contains(CoverableArea.ANUS)) {
			if (Main.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.ANUS, false)) {
				sexSB.append(
						formatCoverableAreaBecomingExposed(
								(atStartOfSex
										?"Your [pc.asshole+] was already exposed before starting sex!"
										:"Your [pc.asshole+] is now exposed!"))
						+ sexManager.getPlayerAssRevealReaction(isPlayerDom())
						+ formatCoverableAreaGettingWet(getLubricationDescription(OrificeType.ANUS_PLAYER)));
				areasExposedPlayer.add(CoverableArea.ANUS);
			}
		}
		if (!areasExposedPlayer.contains(CoverableArea.PENIS)) {
			if (Main.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.PENIS, false)) {
				if (Main.game.getPlayer().getPenisType() != PenisType.NONE) {
					sexSB.append(
						formatCoverableAreaBecomingExposed(
							(atStartOfSex
									?"Your [pc.cock+] was already exposed before starting sex!"
									:"Your [pc.cock+] is now exposed!"))
							+ sexManager.getPlayerPenisRevealReaction(isPlayerDom())
							+ formatCoverableAreaGettingWet(getLubricationDescription(PenetrationType.PENIS_PLAYER)));
				}
				areasExposedPlayer.add(CoverableArea.PENIS);
			}
		}
		if (!areasExposedPlayer.contains(CoverableArea.VAGINA)) {
			if (Main.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.VAGINA, false)) {
				if (Main.game.getPlayer().getVaginaType() != VaginaType.NONE) {
					sexSB.append(
						formatCoverableAreaBecomingExposed(
							(atStartOfSex
									?"Your [pc.pussy+] was already exposed before starting sex!"
									:"Your [pc.pussy+] is now exposed!"))
							+ sexManager.getPlayerVaginaRevealReaction(isPlayerDom())
							+ formatCoverableAreaGettingWet(getLubricationDescription(OrificeType.VAGINA_PLAYER)));

				} else if (Main.game.getPlayer().getVaginaType() == VaginaType.NONE && Main.game.getPlayer().getPenisType() == PenisType.NONE) {
					sexSB.append(
							formatCoverableAreaBecomingExposed(
									(atStartOfSex
											?"Your doll-like mound was already exposed before starting sex!"
											:"Your doll-like mound is now exposed!"))
							+ sexManager.getPlayerMoundRevealReaction(isPlayerDom()));
				}
				areasExposedPlayer.add(CoverableArea.VAGINA);
			}
		}
		if (!areasExposedPlayer.contains(CoverableArea.NIPPLES)) {
			if (Main.game.getPlayer().isAbleToAccessCoverableArea(CoverableArea.NIPPLES, false)) {
				sexSB.append(
						formatCoverableAreaBecomingExposed(
								(atStartOfSex
										?"Your [pc.nipples+] were already exposed before starting sex!"
										:"Your [pc.nipples+] are now exposed!"))
						+ sexManager.getPlayerBreastsRevealReaction(isPlayerDom())
						+ formatCoverableAreaGettingWet(getLubricationDescription(OrificeType.NIPPLE_PLAYER)));
				areasExposedPlayer.add(CoverableArea.NIPPLES);
			}
		}
		
		// Partner:
		if (!areasExposedPartner.contains(CoverableArea.ANUS)) {
			if (partner.isAbleToAccessCoverableArea(CoverableArea.ANUS, false)) {
				sexSB.append(
						formatCoverableAreaBecomingExposed(
								(atStartOfSex
										?"[npc.Name]'s [npc.asshole+] was already exposed before starting sex!"
										:"[npc.Name]'s [npc.asshole+] is now exposed!"))
						+ sexManager.getPartnerAssRevealReaction(isPlayerDom())
						+ formatCoverableAreaGettingWet(getLubricationDescription(OrificeType.ANUS_PARTNER)));
				areasExposedPartner.add(CoverableArea.ANUS);
			}
		}
		if (!areasExposedPartner.contains(CoverableArea.PENIS)) {
			if (partner.isAbleToAccessCoverableArea(CoverableArea.PENIS, false)) {
				if (partner.getPenisType() != PenisType.NONE) {
					sexSB.append(
							formatCoverableAreaBecomingExposed(
									(atStartOfSex
											?"[npc.Name]'s [npc.cock+] was already exposed before starting sex!"
											:"[npc.Name]'s [npc.cock+] is now exposed!"))
							+ sexManager.getPartnerPenisRevealReaction(isPlayerDom())
							+ formatCoverableAreaGettingWet(getLubricationDescription(PenetrationType.PENIS_PARTNER)));
				}
				areasExposedPartner.add(CoverableArea.PENIS);
			}
		}
		if (!areasExposedPartner.contains(CoverableArea.VAGINA)) {
			if (partner.isAbleToAccessCoverableArea(CoverableArea.VAGINA, false)) {
				if (partner.getVaginaType() != VaginaType.NONE) {
					sexSB.append(
							formatCoverableAreaBecomingExposed(
									(atStartOfSex
											?"[npc.Name]'s [npc.pussy+] was already exposed before starting sex!"
											:"[npc.Name]'s [npc.pussy+] is now exposed!"))
							+ sexManager.getPartnerVaginaRevealReaction(isPlayerDom())
							+ formatCoverableAreaGettingWet(getLubricationDescription(OrificeType.VAGINA_PARTNER)));

				} else if (partner.getVaginaType() == VaginaType.NONE && partner.getPenisType() == PenisType.NONE) {
					sexSB.append(
							formatCoverableAreaBecomingExposed(
									(atStartOfSex
											?"[npc.Name]'s doll-like mound was already exposed before starting sex!"
											:"[npc.Name]'s doll-like mound is now exposed!"))
							+ sexManager.getPartnerMoundRevealReaction(isPlayerDom()));
				}
				areasExposedPartner.add(CoverableArea.VAGINA);
			}
		}
		if (!areasExposedPartner.contains(CoverableArea.NIPPLES)) {
			if (partner.isAbleToAccessCoverableArea(CoverableArea.NIPPLES, false)) {
				sexSB.append(
						formatCoverableAreaBecomingExposed(
								(atStartOfSex
										?"[npc.Name]'s [npc.nipples+] were already exposed before starting sex!"
										:"[npc.Name]'s [npc.nipples+] are now exposed!"))
							+ sexManager.getPartnerBreastsRevealReaction(isPlayerDom())
							+ formatCoverableAreaGettingWet(getLubricationDescription(OrificeType.NIPPLE_PARTNER)));
				areasExposedPartner.add(CoverableArea.NIPPLES);
			}
		}
	}

	
	private static void calculateWetAreas(boolean onSexInit) {

		// Add starting lube:
		addOrificeLubrication(OrificeType.MOUTH_PLAYER, LubricationType.PLAYER_SALIVA);
		addPenetrationTypeLubrication(PenetrationType.TONGUE_PLAYER, LubricationType.PLAYER_SALIVA);
		addOrificeLubrication(OrificeType.MOUTH_PARTNER, LubricationType.PARTNER_SALIVA);
		addPenetrationTypeLubrication(PenetrationType.TONGUE_PARTNER, LubricationType.PARTNER_SALIVA);
		
		// Add milk in nipples:
		if(Main.game.getPlayer().getBreastRawLactationValue()>0) {
			addOrificeLubrication(OrificeType.NIPPLE_PLAYER, LubricationType.PLAYER_MILK);
		}
		if(partner.getBreastRawLactationValue()>0) {
			addOrificeLubrication(OrificeType.NIPPLE_PARTNER, LubricationType.PARTNER_MILK);
		}
		
		// Add player lubrication from cum:
		if(Main.game.getPlayer().hasStatusEffect(StatusEffect.CREAMPIE_ANUS)) {
			addOrificeLubrication(OrificeType.ANUS_PLAYER, LubricationType.OTHER_CUM);
		}
		if(Main.game.getPlayer().hasStatusEffect(StatusEffect.CREAMPIE_NIPPLES)) {
			addOrificeLubrication(OrificeType.NIPPLE_PLAYER, LubricationType.OTHER_CUM);
		}
		if(Main.game.getPlayer().hasStatusEffect(StatusEffect.CREAMPIE_VAGINA)) {
			addOrificeLubrication(OrificeType.VAGINA_PLAYER, LubricationType.OTHER_CUM);
		}

		// Add partner lubrication from cum:
		if(partner.hasStatusEffect(StatusEffect.CREAMPIE_ANUS)) {
			addOrificeLubrication(OrificeType.ANUS_PARTNER, LubricationType.OTHER_CUM);
		}
		if(partner.hasStatusEffect(StatusEffect.CREAMPIE_NIPPLES)) {
			addOrificeLubrication(OrificeType.NIPPLE_PARTNER, LubricationType.OTHER_CUM);
		}
		if(partner.hasStatusEffect(StatusEffect.CREAMPIE_VAGINA)) {
			addOrificeLubrication(OrificeType.VAGINA_PARTNER, LubricationType.OTHER_CUM);
		}

		// Add player natural lubrications:
		if(Main.game.getPlayer().getArousal() >= Main.game.getPlayer().getAssWetness().getArousalNeededToGetVaginaWet()) {
			addOrificeLubrication(OrificeType.ANUS_PLAYER, LubricationType.PLAYER_NATURAL_LUBRICATION);
		}
		if(Main.game.getPlayer().hasPenis()) {
			if(Main.game.getPlayer().getArousal() >= Main.game.getPlayer().getPenisCumProduction().getArousalNeededToStartPreCumming()) {
				addPenetrationTypeLubrication(PenetrationType.PENIS_PLAYER, LubricationType.PLAYER_PRECUM);
				addOrificeLubrication(OrificeType.URETHRA_PLAYER, LubricationType.PLAYER_PRECUM);
			}
		}
		if(Main.game.getPlayer().hasVagina()) {
			if(Main.game.getPlayer().getArousal() >= Main.game.getPlayer().getVaginaWetness().getArousalNeededToGetVaginaWet()) {
				addOrificeLubrication(OrificeType.VAGINA_PLAYER, LubricationType.PLAYER_NATURAL_LUBRICATION);
			}
		}
		
		// Add partner natural lubrications:
		if(partner.getArousal() >= partner.getAssWetness().getArousalNeededToGetVaginaWet()) {
			addOrificeLubrication(OrificeType.ANUS_PARTNER, LubricationType.PARTNER_NATURAL_LUBRICATION);
		}
		if(partner.hasPenis()) {
			if(partner.getArousal() >= partner.getPenisCumProduction().getArousalNeededToStartPreCumming()) {
				addPenetrationTypeLubrication(PenetrationType.PENIS_PARTNER, LubricationType.PARTNER_PRECUM);
				addOrificeLubrication(OrificeType.URETHRA_PARTNER, LubricationType.PARTNER_PRECUM);
			}
		}
		if(partner.hasVagina()) {
			if(partner.getArousal() >= partner.getVaginaWetness().getArousalNeededToGetVaginaWet()) {
				addOrificeLubrication(OrificeType.VAGINA_PARTNER, LubricationType.PARTNER_NATURAL_LUBRICATION);
			}
		}

		// Calculate lubrication transfers:
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				transferLubrication(
						(entry.getKey().isPlayer()?Main.game.getPlayer():partner),
						(ot.isPlayer()?Main.game.getPlayer():partner),
						entry.getKey(),
						ot);
			}
		}
	}
	
	public static void transferLubrication(GameCharacter penetrator, GameCharacter penetrated, PenetrationType penetrationType, OrificeType orificeType) {
		List<String> lubricationTransferred = new ArrayList<>();
		boolean lastLubricationPlural = false;
		
		for(LubricationType lt : wetPenetrationTypes.get(penetrationType)) {
			if(!wetOrificeTypes.get(orificeType).contains(lt)) {
				wetOrificeTypes.get(orificeType).add(lt);
				lubricationTransferred.add(lt.getName());
				if(lt.isPlural()) {
					lastLubricationPlural = true;
				} else {
					lastLubricationPlural = false;
				}
			}
		}
		
		if(!lubricationTransferred.isEmpty()) {
			sexSB.append(formatCoverableAreaGettingWet(
					Util.capitaliseSentence(Util.stringsToStringList(lubricationTransferred, false))+" quickly lubricate"+(lastLubricationPlural?" ":"s ")
						+(orificeType.isPlayer()?"your ":partner.getName("the")+"'s ")+orificeType.getName()+"."));
		}
		
		lubricationTransferred.clear();
		
		for(LubricationType lt : wetOrificeTypes.get(orificeType)) {
			if(!wetPenetrationTypes.get(penetrationType).contains(lt)) {
				wetPenetrationTypes.get(penetrationType).add(lt);
				lubricationTransferred.add(lt.getName());
				if(lt.isPlural()) {
					lastLubricationPlural = true;
				} else {
					lastLubricationPlural = false;
				}
			}
		}
		
		if(!lubricationTransferred.isEmpty()) {
			sexSB.append(formatCoverableAreaGettingWet(
					Util.capitaliseSentence(Util.stringsToStringList(lubricationTransferred, false))+" quickly lubricate"+(lastLubricationPlural?" ":"s ")
						+(penetrationType.isPlayer()?"your ":partner.getName("the")+"'s ")+penetrationType.getName()+"."));
		}
	}
	
	private static String getLubricationDescription(OrificeType orifice) {
		if(wetOrificeTypes.get(orifice).isEmpty()) {
			return "";
		}
		StringBuilder description = new StringBuilder(
				(orifice.isPlayer()?"Your "+orifice.getName():"[npc.Name]'s "+orifice.getName()) +" "+(orifice.isPlural()?"are":"is")+" lubricated with ");
		List<String> lubes = new ArrayList<>();
		for(LubricationType lube : wetOrificeTypes.get(orifice)) {
			lubes.add(lube.getName());
		}
		description.append(Util.stringsToStringList(lubes, false)+".");
		return description.toString();
	}
	
	private static String getLubricationDescription(PenetrationType penetration) {
		if(wetPenetrationTypes.get(penetration).isEmpty()) {
			return "";
		}
		StringBuilder description = new StringBuilder(
				(penetration.isPlayer()?"Your "+penetration.getName():"[npc.Name]'s "+penetration.getName()) +" "+(penetration.isPlural()?"are":"is")+" lubricated with ");
		List<String> lubes = new ArrayList<>();
		for(LubricationType lube : wetPenetrationTypes.get(penetration)) {
			lubes.add(lube.getName());
		}
		description.append(Util.stringsToStringList(lubes, false)+".");
		return description.toString();
	}
	
	public static void addOrificeLubrication(OrificeType orifice, LubricationType lubrication) {
		boolean appendDescription =
				orifice != OrificeType.URETHRA_PLAYER && orifice != OrificeType.URETHRA_PARTNER// Can't penetrate urethras for now, so skip that description.
				&& !(orifice==OrificeType.MOUTH_PLAYER && lubrication==LubricationType.PLAYER_SALIVA) // Don't give descriptions of saliva lubricating your own mouth.
				&& !(orifice==OrificeType.MOUTH_PARTNER && lubrication==LubricationType.PARTNER_SALIVA)
				&& (orifice==OrificeType.VAGINA_PARTNER?partner.isCoverableAreaExposed(CoverableArea.VAGINA):true)
				&& (orifice==OrificeType.ANUS_PARTNER?partner.isCoverableAreaExposed(CoverableArea.ANUS):true)
				&& (orifice==OrificeType.NIPPLE_PARTNER?partner.isCoverableAreaExposed(CoverableArea.NIPPLES):true);
		
		if(orifice.isPlayer()) {
			if(wetOrificeTypes.get(orifice).add(lubrication)){
				if(appendDescription) {
					sexSB.append(formatCoverableAreaGettingWet("Your "+orifice.getName()+" "+(orifice.isPlural()?"are":"is")+" quickly lubricated by "+lubrication.getName()+"."));
				}
			}
		} else {
			if(wetOrificeTypes.get(orifice).add(lubrication)){
				if(appendDescription) {
					sexSB.append(formatCoverableAreaGettingWet("[npc.Name]'s "+orifice.getName()+" "+(orifice.isPlural()?"are":"is")+" quickly lubricated by "+lubrication.getName()+"."));
				}
			}
		}
	}
	
	public static void addPenetrationTypeLubrication(PenetrationType penetrationType, LubricationType lubrication) {
		boolean appendDescription =
				!(penetrationType==PenetrationType.TONGUE_PLAYER && lubrication==LubricationType.PLAYER_SALIVA) // Don't give descriptions of saliva lubricating your own tongue.
				&& !(penetrationType==PenetrationType.TONGUE_PARTNER && lubrication==LubricationType.PARTNER_SALIVA)
				&& (penetrationType==PenetrationType.PENIS_PARTNER?partner.isCoverableAreaExposed(CoverableArea.PENIS):true);
		
		if(penetrationType.isPlayer()) {
			if((penetrationType == PenetrationType.PENIS_PLAYER && (lubrication == LubricationType.PLAYER_PRECUM || lubrication == LubricationType.PLAYER_CUM) ? !Main.game.getPlayer().isWearingCondom() : true)) { // Can't lubricate if covered by condom
				if(wetPenetrationTypes.get(penetrationType).add(lubrication)){
					if(appendDescription) {
						sexSB.append(formatCoverableAreaGettingWet("Your "+penetrationType.getName()+" "+(penetrationType.isPlural()?"are":"is")+" quickly lubricated by "+lubrication.getName()+"."));
					}
				}
			}
		} else {
			if((penetrationType == PenetrationType.PENIS_PARTNER && (lubrication == LubricationType.PARTNER_PRECUM || lubrication == LubricationType.PARTNER_CUM) ? !partner.isWearingCondom() : true)) { // Can't lubricate if covered by condom
				if(wetPenetrationTypes.get(penetrationType).add(lubrication)){
					if(appendDescription) {
						sexSB.append(formatCoverableAreaGettingWet("[npc.Name]'s "+penetrationType.getName()+" "+(penetrationType.isPlural()?"are":"is")+" quickly lubricated by "+lubrication.getName()+"."));
					}
				}
			}
		}
	}


//	private static boolean initialAnalPenetration = false, initialVaginalPenetration = false, initialNipplePenetration = false, initialPenisPenetration = false, initialMouthPenetration = false;
	private static Set<OrificeType> initialPenetrations = new HashSet<>();

	public static void applyPenetration(PenetrationType penetration, OrificeType orifice) {
		
		GameCharacter characterPenetrated = null, characterPenetrating = null;
		SexType relatedSexType = new SexType(penetration, orifice);
		if (orifice.isPlayer()){
			characterPenetrated = Main.game.getPlayer();
			characterPenetrating = Sex.getPartner();
		} else {
			characterPenetrated = Sex.getPartner();
			characterPenetrating = Main.game.getPlayer();
		}
		
		
		// Free up orifice and penetrator:
		removePenetration(penetration, orifice);
		
		if(ongoingPenetrationMap.containsKey(penetration)) {
			ongoingPenetrationMap.get(penetration).add(orifice);
		} else {
			ongoingPenetrationMap.put(penetration, new HashSet<>());
			ongoingPenetrationMap.get(penetration).add(orifice);
		}
		
		initialPenetrations.add(orifice);
		
		if(characterPenetrated != null && characterPenetrating != null) {
			characterPenetrated.incrementSexCount(relatedSexType);
			characterPenetrating.incrementSexCount(relatedSexType);
			
			characterPenetrating.addSexPartner(characterPenetrated, relatedSexType);
			characterPenetrated.addSexPartner(characterPenetrating, relatedSexType);
			
		} else {
			System.err.println("Warning! Sex.applyPenetration() is finding 'characterPenetrated' or 'characterPenetrating' to be null!!!");
		}
	}

	public static void removePenetration(PenetrationType penetrationType, OrificeType orifice) {
		removePenetration(true, penetrationType, orifice);
	}
	
	public static void removePenetration(boolean appendRemovalText, PenetrationType penetrationType, OrificeType orifice) {
		if(ongoingPenetrationMap.containsKey(penetrationType)) {
			if(ongoingPenetrationMap.get(penetrationType).remove(orifice)) {
				if(appendRemovalText) {
					sexSB.append(formatStopPenetration(partner.getStopPenetrationDescription(penetrationType, orifice)));
				}
			}
			
			if(ongoingPenetrationMap.get(penetrationType).isEmpty()) {
				ongoingPenetrationMap.remove(penetrationType);
			}
		}
		
	}

	private static void applyPenetrationEffects(PenetrationType penetrationType, OrificeType orifice) { //TODO formatting

		SexType relatedSexType = new SexType(penetrationType, orifice);
		
		if (penetrationType == PenetrationType.PENIS_PLAYER) {
			if(Main.game.getPlayer().isPenisVirgin()) {
				sexSB.append(partner.getPlayerPenileVirginityLossDescription());
				if(partner.hasFetish(Fetish.FETISH_DEFLOWERING)) {
					partner.incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(partner));
				}
				Main.game.getPlayer().setVirginityLoss(relatedSexType, partner.getName("a") + " " + partner.getLostVirginityDescriptor());
				Main.game.getPlayer().setPenisVirgin(false);
			}
			
		} else if (penetrationType == PenetrationType.PENIS_PARTNER) {
			if(partner.isPenisVirgin()) {
				sexSB.append(partner.getPartnerPenileVirginityLossDescription());
				if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DEFLOWERING)) {
					Main.game.getPlayer().incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(Main.game.getPlayer()));
				}
				partner.setPenisVirgin(false);
			}
		}
		
		// Append description based on what orifice is being penetrated and by whom:
		
		if (orifice == OrificeType.ANUS_PLAYER) {
			if (initialPenetrations.contains(OrificeType.ANUS_PLAYER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (Main.game.getPlayer().isAssVirgin()) {
					if (penetrationType.isTakesVirginity()) {
						sexSB.append(partner.getPlayerAnalVirginityLossDescription());
						if(partner.hasFetish(Fetish.FETISH_DEFLOWERING)) {
							partner.incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(partner));
						}
						Main.game.getPlayer().setVirginityLoss(relatedSexType, partner.getName("a") + " " + partner.getLostVirginityDescriptor());
						Main.game.getPlayer().setAssVirgin(false);
					}
				}
				initialPenetrations.remove(OrificeType.ANUS_PLAYER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}
				
		} else if (orifice == OrificeType.ANUS_PARTNER) {
			if (initialPenetrations.contains(OrificeType.ANUS_PARTNER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (partner.isAssVirgin()) {
					if (penetrationType.isTakesVirginity()) {
						sexSB.append(partner.getPartnerAnalVirginityLossDescription());
						if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DEFLOWERING)) {
							Main.game.getPlayer().incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(Main.game.getPlayer()));
						}
						partner.setAssVirgin(false);
					}
				}
				initialPenetrations.remove(OrificeType.ANUS_PARTNER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}
			
		} else if (orifice == OrificeType.VAGINA_PLAYER) {
			if (initialPenetrations.contains(OrificeType.VAGINA_PLAYER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (Main.game.getPlayer().isVaginaVirgin()) {
						if (penetrationType.isTakesVirginity()) {
							sexSB.append(partner.getPlayerVaginaVirginityLossDescription(isPlayerDom()));
							if(partner.hasFetish(Fetish.FETISH_DEFLOWERING)) {
								partner.incrementExperience(Fetish.getExperienceGainFromTakingVaginalVirginity(partner));
							}
							Main.game.getPlayer().setVirginityLoss(relatedSexType, partner.getName("a") + " " + partner.getLostVirginityDescriptor());
							Main.game.getPlayer().setVaginaVirgin(false);
						}
				}
				initialPenetrations.remove(OrificeType.VAGINA_PLAYER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}
			
		} else if (orifice == OrificeType.VAGINA_PARTNER) {
			if (initialPenetrations.contains(OrificeType.VAGINA_PARTNER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (partner.isVaginaVirgin()) {
					if (penetrationType.isTakesVirginity()) {
						sexSB.append(partner.getPartnerVaginaVirginityLossDescription());
						if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DEFLOWERING)) {
							Main.game.getPlayer().incrementExperience(Fetish.getExperienceGainFromTakingVaginalVirginity(Main.game.getPlayer()));
						}
						partner.setVaginaVirgin(false);
					}
				}
				initialPenetrations.remove(OrificeType.VAGINA_PARTNER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}
			
		} else if (orifice == OrificeType.NIPPLE_PLAYER) {
			// Penetrating player's nipples:
			if (initialPenetrations.contains(OrificeType.NIPPLE_PLAYER)) {
					sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
					
					if (Main.game.getPlayer().isNippleVirgin()) {
						if (penetrationType.isTakesVirginity()) {
							sexSB.append(partner.getPlayerNippleVirginityLossDescription());
							if(partner.hasFetish(Fetish.FETISH_DEFLOWERING)) {
								partner.incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(partner));
							}
							Main.game.getPlayer().setVirginityLoss(relatedSexType, partner.getName("a") + " " + partner.getLostVirginityDescriptor());
							Main.game.getPlayer().setNippleVirgin(false);
						}
					}
					initialPenetrations.remove(OrificeType.NIPPLE_PLAYER);
				} else {
					sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
				}
				
		} else if (orifice == OrificeType.NIPPLE_PARTNER) {
			if (initialPenetrations.contains(OrificeType.NIPPLE_PARTNER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (partner.isNippleVirgin()) {
					if (penetrationType.isTakesVirginity()) {
						sexSB.append(partner.getPartnerNippleVirginityLossDescription());
						if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DEFLOWERING)) {
							Main.game.getPlayer().incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(Main.game.getPlayer()));
						}
						partner.setNippleVirgin(false);
					}
				}
				initialPenetrations.remove(OrificeType.NIPPLE_PARTNER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}
		
		} else if (orifice == OrificeType.URETHRA_PLAYER) {
			// Penetrating player's urethra:
			if (initialPenetrations.contains(OrificeType.URETHRA_PLAYER)) {
					sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
					
					if (Main.game.getPlayer().isUrethraVirgin()) {
						if (penetrationType.isTakesVirginity()) {
							sexSB.append(partner.getPlayerUrethraVirginityLossDescription());
							if(partner.hasFetish(Fetish.FETISH_DEFLOWERING)) {
								partner.incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(partner));
							}
							Main.game.getPlayer().setVirginityLoss(relatedSexType, partner.getName("a") + " " + partner.getLostVirginityDescriptor());
							Main.game.getPlayer().setUrethraVirgin(false);
						}
					}
					initialPenetrations.remove(OrificeType.URETHRA_PLAYER);
				} else {
					sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
				}
			
		} else if (orifice == OrificeType.URETHRA_PARTNER) {
			if (initialPenetrations.contains(OrificeType.URETHRA_PARTNER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (partner.isUrethraVirgin()) {
					if (penetrationType.isTakesVirginity()) {
						sexSB.append(partner.getPartnerUrethraVirginityLossDescription());
						if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DEFLOWERING)) {
							Main.game.getPlayer().incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(Main.game.getPlayer()));
						}
						partner.setUrethraVirgin(false);
					}
				}
				initialPenetrations.remove(OrificeType.URETHRA_PARTNER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}

		} else if (orifice == OrificeType.MOUTH_PLAYER) {
			// Penetrating player's mouth:
			if (initialPenetrations.contains(OrificeType.MOUTH_PLAYER)) {
					sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
					
					if (Main.game.getPlayer().isFaceVirgin()) {
						if (penetrationType.isTakesVirginity()) {
							sexSB.append(partner.getPlayerMouthVirginityLossDescription());
							if(partner.hasFetish(Fetish.FETISH_DEFLOWERING)) {
								partner.incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(partner));
							}
							Main.game.getPlayer().setVirginityLoss(relatedSexType, partner.getName("a") + " " + partner.getLostVirginityDescriptor());
							Main.game.getPlayer().setFaceVirgin(false);
						}
					}
					initialPenetrations.remove(OrificeType.MOUTH_PLAYER);
				} else {
					sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
				}
				
		} else if (orifice == OrificeType.MOUTH_PARTNER) {
			if (initialPenetrations.contains(OrificeType.MOUTH_PARTNER)) {
				sexSB.append(formatInitialPenetration(partner.getPenetrationDescription(true, penetrationType, orifice)));
				
				if (partner.isFaceVirgin()) {
					if (penetrationType.isTakesVirginity()) {
						sexSB.append(partner.getPartnerMouthVirginityLossDescription());
						if(Main.game.getPlayer().hasFetish(Fetish.FETISH_DEFLOWERING)) {
							Main.game.getPlayer().incrementExperience(Fetish.getExperienceGainFromTakingOtherVirginity(Main.game.getPlayer()));
						}
						partner.setFaceVirgin(false);
					}
				}
				initialPenetrations.remove(OrificeType.MOUTH_PARTNER);
			} else {
				sexSB.append(formatPenetration(partner.getPenetrationDescription(false, penetrationType, orifice)));
			}
			
		}

		// TODO apply masochism effects to stretching:

		// Stretching effects (will only stretch from penises):
		if (penetrationType.isPenis()) {
			
			GameCharacter personPenetrating = Main.game.getPlayer();
			if(!penetrationType.isPlayer()) {
				personPenetrating = partner;
			}
			
			boolean lubed = !wetOrificeTypes.get(orifice).isEmpty();
			boolean twoPenisesInOrifice = false;

			if (orifice.isPlayer()) {
				areasCurrentlyStretchingPlayer.clear();
				if (orifice == OrificeType.ANUS_PLAYER){
					if (Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getAssStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerAssStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						Main.game.getPlayer().incrementAssStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-Main.game.getPlayer().getAssStretchedCapacity())*Main.game.getPlayer().getAssElasticity().getStretchModifier());
						if(Main.game.getPlayer().getAssStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							Main.game.getPlayer().setAssStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPlayer.add(OrificeType.ANUS_PLAYER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getAssStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPlayerAssStretchingFinishedDescription());
							areasCurrentlyStretchingPlayer.remove(OrificeType.ANUS_PLAYER);
						}

						areasStretchedPlayer.add(OrificeType.ANUS_PLAYER);

					}else if(Capacity.isPenisSizeTooSmall((int)Main.game.getPlayer().getAssStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerAssTooLooseDescription());
						areasTooLoosePlayer.add(OrificeType.ANUS_PLAYER);
					}

				} else if (orifice == OrificeType.VAGINA_PLAYER){
					if (Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getVaginaStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerVaginaStretchingDescription(penetrationType));
						
						// Stretch out the orifice by a factor of elasticity's modifier.
						Main.game.getPlayer().incrementVaginaStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-Main.game.getPlayer().getVaginaStretchedCapacity())*Main.game.getPlayer().getVaginaElasticity().getStretchModifier());
						if(Main.game.getPlayer().getVaginaStretchedCapacity()>personPenetrating.getPenisRawSizeValue()) {
							Main.game.getPlayer().setVaginaStretchedCapacity(personPenetrating.getPenisRawSizeValue());
						}
						
						areasCurrentlyStretchingPlayer.add(OrificeType.VAGINA_PLAYER);
						
						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getVaginaStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPlayerVaginaStretchingFinishedDescription());
							areasCurrentlyStretchingPlayer.remove(OrificeType.VAGINA_PLAYER);
						}

						areasStretchedPlayer.add(OrificeType.VAGINA_PLAYER);

					} else if(Capacity.isPenisSizeTooSmall((int)Main.game.getPlayer().getVaginaStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerVaginaTooLooseDescription());
						areasTooLoosePlayer.add(OrificeType.VAGINA_PLAYER);
					}

				}else if (orifice == OrificeType.NIPPLE_PLAYER){
					if (Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getNippleStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerBreastsStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						Main.game.getPlayer().incrementNippleStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-Main.game.getPlayer().getNippleStretchedCapacity())*Main.game.getPlayer().getNippleElasticity().getStretchModifier());
						if(Main.game.getPlayer().getNippleStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							Main.game.getPlayer().setNippleStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPlayer.add(OrificeType.NIPPLE_PLAYER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getNippleStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPlayerBreastsStretchingFinishedDescription());
							areasCurrentlyStretchingPlayer.remove(OrificeType.NIPPLE_PLAYER);
						}

						areasStretchedPlayer.add(OrificeType.NIPPLE_PLAYER);

					}else if(Capacity.isPenisSizeTooSmall((int)Main.game.getPlayer().getNippleStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerBreastsTooLooseDescription());
						areasTooLoosePlayer.add(OrificeType.NIPPLE_PLAYER);
					}

				}else if (orifice == OrificeType.URETHRA_PLAYER){
					if (Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getPenisStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerPenisStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						Main.game.getPlayer().incrementPenisStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-Main.game.getPlayer().getPenisStretchedCapacity())*Main.game.getPlayer().getUrethraElasticity().getStretchModifier());
						if(Main.game.getPlayer().getPenisStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							Main.game.getPlayer().setPenisStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPlayer.add(OrificeType.URETHRA_PLAYER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getPenisStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPlayerPenisStretchingFinishedDescription());
							areasCurrentlyStretchingPlayer.remove(OrificeType.URETHRA_PLAYER);
						}

						areasStretchedPlayer.add(OrificeType.URETHRA_PLAYER);

					}else if(Capacity.isPenisSizeTooSmall((int)Main.game.getPlayer().getPenisStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerPenisTooLooseDescription());
						areasTooLoosePlayer.add(OrificeType.URETHRA_PLAYER);
					}

				}else if (orifice == OrificeType.MOUTH_PLAYER){
					if (Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getFaceStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerMouthStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						Main.game.getPlayer().incrementFaceStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-Main.game.getPlayer().getFaceStretchedCapacity())*Main.game.getPlayer().getFaceElasticity().getStretchModifier());
						if(Main.game.getPlayer().getFaceStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							Main.game.getPlayer().setFaceStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPlayer.add(OrificeType.MOUTH_PLAYER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)Main.game.getPlayer().getFaceStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPlayerMouthStretchingFinishedDescription());
							areasCurrentlyStretchingPlayer.remove(OrificeType.MOUTH_PLAYER);
						}

						areasStretchedPlayer.add(OrificeType.MOUTH_PLAYER);

					}else if(Capacity.isPenisSizeTooSmall((int)Main.game.getPlayer().getFaceStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPlayerMouthTooLooseDescription());
					}
				}

			} else {
				areasCurrentlyStretchingPartner.clear();
				if (orifice == OrificeType.ANUS_PARTNER){
					if (Capacity.isPenisSizeTooBig((int)partner.getAssStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerAssStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						partner.incrementAssStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-partner.getAssStretchedCapacity())*partner.getAssElasticity().getStretchModifier());
						if(partner.getAssStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							partner.setAssStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPartner.add(OrificeType.ANUS_PARTNER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)partner.getAssStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPartnerAssStretchingFinishedDescription());
							areasCurrentlyStretchingPartner.remove(OrificeType.ANUS_PARTNER);
						}

						areasStretchedPartner.add(OrificeType.ANUS_PARTNER);

					}else if(Capacity.isPenisSizeTooSmall((int)partner.getAssStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerAssTooLooseDescription());
						areasTooLoosePartner.add(OrificeType.ANUS_PARTNER);
					}

				}else if (orifice == OrificeType.VAGINA_PARTNER){
					if (Capacity.isPenisSizeTooBig((int)partner.getVaginaStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerVaginaStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						partner.incrementVaginaStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-partner.getVaginaStretchedCapacity())*partner.getVaginaElasticity().getStretchModifier());
						if(partner.getVaginaStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							partner.setVaginaStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPartner.add(OrificeType.VAGINA_PARTNER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)partner.getVaginaStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPartnerVaginaStretchingFinishedDescription());
							areasCurrentlyStretchingPartner.remove(OrificeType.VAGINA_PARTNER);
						}

						areasStretchedPartner.add(OrificeType.VAGINA_PARTNER);

					}else if(Capacity.isPenisSizeTooSmall((int)partner.getVaginaStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerVaginaTooLooseDescription());
						areasTooLoosePartner.add(OrificeType.VAGINA_PARTNER);
					}

				}else if (orifice == OrificeType.NIPPLE_PARTNER){
					if (Capacity.isPenisSizeTooBig((int)partner.getNippleStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerBreastsStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						partner.incrementNippleStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-partner.getNippleStretchedCapacity())*partner.getNippleElasticity().getStretchModifier());
						if(partner.getNippleStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							partner.setNippleStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPartner.add(OrificeType.NIPPLE_PARTNER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)partner.getNippleStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPartnerBreastsStretchingFinishedDescription());
							areasCurrentlyStretchingPartner.remove(OrificeType.NIPPLE_PARTNER);
						}

						areasStretchedPartner.add(OrificeType.NIPPLE_PARTNER);

					}else if(Capacity.isPenisSizeTooSmall((int)partner.getNippleStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerBreastsTooLooseDescription());
						areasTooLoosePartner.add(OrificeType.NIPPLE_PARTNER);
					}

				}else if (orifice == OrificeType.URETHRA_PARTNER){
					if (Capacity.isPenisSizeTooBig((int)partner.getPenisStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerPenisStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						partner.incrementPenisStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-partner.getPenisStretchedCapacity())*partner.getUrethraElasticity().getStretchModifier());
						if(partner.getPenisStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							partner.setPenisStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPartner.add(OrificeType.URETHRA_PARTNER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)partner.getPenisStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPartnerPenisStretchingFinishedDescription());
							areasCurrentlyStretchingPartner.remove(OrificeType.URETHRA_PARTNER);
						}

						areasStretchedPartner.add(OrificeType.URETHRA_PARTNER);

					}else if(Capacity.isPenisSizeTooSmall((int)partner.getPenisStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerPenisTooLooseDescription());
						areasTooLoosePartner.add(OrificeType.URETHRA_PARTNER);
					}

				}else if (orifice == OrificeType.MOUTH_PARTNER){
					if (Capacity.isPenisSizeTooBig((int)partner.getFaceStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerMouthStretchingDescription(penetrationType));

						// Stretch out the orifice by a factor of elasticity's modifier.
						partner.incrementFaceStretchedCapacity((((float)personPenetrating.getPenisRawSizeValue())-partner.getFaceStretchedCapacity())*partner.getFaceElasticity().getStretchModifier());
						if(partner.getFaceStretchedCapacity()>personPenetrating.getPenisRawSizeValue())
							partner.setFaceStretchedCapacity(personPenetrating.getPenisRawSizeValue());

						areasCurrentlyStretchingPartner.add(OrificeType.MOUTH_PARTNER);

						// If just stretched out enough to be comfortable, append that description:
						if(!Capacity.isPenisSizeTooBig((int)partner.getFaceStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), lubed, twoPenisesInOrifice)) {
							sexSB.append(partner.getPartnerMouthStretchingFinishedDescription());
							areasCurrentlyStretchingPartner.remove(OrificeType.MOUTH_PARTNER);
						}

						areasStretchedPartner.add(OrificeType.MOUTH_PARTNER);

					}else if(Capacity.isPenisSizeTooSmall((int)partner.getFaceStretchedCapacity(), personPenetrating.getPenisRawSizeValue(), twoPenisesInOrifice)){
						sexSB.append(partner.getPartnerMouthTooLooseDescription());
					}
				}
			}
		}
	}

	/**
	 * Removes a piece of clothing.
	 *
	 * @return SexActionUtility.CLOTHING_REMOVAL
	 */
	public static SexActionInterface partnerManageClothingToAccessCoverableArea(boolean playerClothing, CoverableArea coverableArea) {
		if (playerClothing) {
			SimpleEntry<AbstractClothing, DisplacementType> clothingRemoval = Main.game.getPlayer().getNextClothingToRemoveForCoverableAreaAccess(coverableArea);
			if (clothingRemoval.getKey() == null) {
				throw new NullPointerException("No clothing found to remove!");
			}
			
			clothingBeingRemoved = clothingRemoval.getKey();

			if (clothingRemoval.getValue() == DisplacementType.REMOVE_OR_EQUIP) {// || player().isAbleToUnequip(clothingRemoval.getKey(), false, partner)) {
				player().unequipClothingOntoFloor(clothingBeingRemoved, false, getPartner());
				unequipClothingText = Main.game.getPlayer().getUnequipDescription();

			} else {
				player().isAbleToBeDisplaced(clothingBeingRemoved, clothingRemoval.getValue(), true, false, getPartner());
				unequipClothingText = Main.game.getPlayer().getDisplaceDescription();
			}

		} else {
			SimpleEntry<AbstractClothing, DisplacementType> clothingRemoval = partner.getNextClothingToRemoveForCoverableAreaAccess(coverableArea);
			if (clothingRemoval.getKey() == null)
				throw new NullPointerException("No clothing found to remove!");

			clothingBeingRemoved = clothingRemoval.getKey();

			if (clothingRemoval.getValue() == DisplacementType.REMOVE_OR_EQUIP) {//  || partner.isAbleToUnequip(clothingRemoval.getKey(), false, partner)) {
				partner.unequipClothingOntoFloor(clothingBeingRemoved, false, getPartner());
				unequipClothingText = partner.getUnequipDescription();

			} else {
				partner.isAbleToBeDisplaced(clothingBeingRemoved, clothingRemoval.getValue(), true, false, getPartner());
				unequipClothingText = partner.getDisplaceDescription();
			}
		}

		return SexActionUtility.CLOTHING_REMOVAL;
	}

	public static boolean isInForeplay() {
		return Sex.getPartner().getArousal()<ArousalLevel.ONE_TURNED_ON.getMaximumValue() && Sex.getNumberOfPartnerOrgasms()==0;
	}
	
	// Getters & Setters:

	public static boolean isConsensual() {
		return consensual;
	}

	public static void setConsensual(boolean consensual) {
		Sex.consensual = consensual;
	}

	public static boolean isSubHasEqualControl() {
		return subHasEqualControl;
	}

	public static void setCanResist(boolean subHasEqualControl) {
		Sex.subHasEqualControl = subHasEqualControl;
	}

	public static NPC getPartner() {
		return partner;
	}

	private static PlayerCharacter player() {
		return Main.game.getPlayer();
	}

	public static String getUnequipClothingText() {
		return unequipClothingText;
	}

	public static void setUnequipClothingText(String unequipClothingText) {
		Sex.unequipClothingText = unequipClothingText;
	}
	
	public static String getDyeClothingText() {
		return dyeClothingText;
	}
	
	public static void setDyeClothingText(String dyeClothingText) {
		Sex.dyeClothingText = dyeClothingText;
	}

	public static String getUsingItemText() {
		return usingItemText;
	}

	public static void setUsingItemText(String usingItemText) {
		Sex.usingItemText = usingItemText;
	}

	public static AbstractClothing getClothingBeingRemoved() {
		return clothingBeingRemoved;
	}
//	public static boolean isPlayerWet(String p) {
//		CoverableArea area = CoverableArea.valueOf(p.toUpperCase());
//		return getWetAreasPlayer().containsKey(area);
//	}
//	public static boolean isPlayerPenetrated(String p) {
//		PenetrationType area = PenetrationType.valueOf(p.toUpperCase(Locale.ENGLISH));
//		return getPlayerPenetratedMap().containsKey(area);
//	}
//	public static Map<CoverableArea, PenetrationType> getPlayerPenetratedMap() {
//		return playerPenetratedMap;
//	}
//	public static String playerPenetratedBy(String p) {
//		CoverableArea area = CoverableArea.valueOf(p.toUpperCase());
//		return getPlayerPenetratedMap().get(area).name();
//	}
//	public static boolean isPartnerWet(String p) {
//		CoverableArea area = CoverableArea.valueOf(p.toUpperCase());
//		return getWetAreasPartner().containsKey(area);
//	}
//	public static boolean isPartnerPenetrated(String p) {
//		PenetrationType area = PenetrationType.valueOf(p.toUpperCase(Locale.ENGLISH));
//		return getPartnerPenetratedMap().containsKey(area);
//	}
//	public static Map<CoverableArea, PenetrationType> getPartnerPenetratedMap() {
//		return partnerPenetratedMap;
//	}
//	public String partnerPenetratedBy(String p) {
//		CoverableArea area = CoverableArea.valueOf(p.toUpperCase());
//		return getPartnerPenetratedMap().get(area).name();
//	}
	
	public static boolean isAnyPenetrationHappening() {
		return isPlayerPenetrated() || isPartnerPenetrated();
	}
	
	public static boolean isPlayerPenetrated() {
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				if(ot.isPlayer())
					return true;
			}
		}
		
		return false;
	}
	public static boolean isPartnerPenetrated() {
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				if(!ot.isPlayer())
					return true;
			}
		}
		
		return false;
	}

	public static boolean isAnyNonSelfPenetrationHappening() {
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				if(entry.getKey().isPlayer() != ot.isPlayer())
					return true;
			}
		}
		
		return false;
	}


	public static boolean isPlayerSelfPenetrationHappening() {
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				if(entry.getKey().isPlayer() && ot.isPlayer())
					return true;
			}
		}
		
		return false;
	}
	public static boolean isPartnerSelfPenetrationHappening() {
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				if(!entry.getKey().isPlayer() && !ot.isPlayer())
					return true;
			}
		}
		
		return false;
	}


	// Free area convenience methods:

	// Player vagina:
	public static boolean isPlayerFreeVagina() {
		return getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER)==null;
	}
	public static boolean isPlayerVaginaNoPartnerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.VAGINA_PLAYER).isPlayer();
		}
	}
	
	// Player penis:
	public static boolean isPlayerFreePenis() {
		return !ongoingPenetrationMap.containsKey(PenetrationType.PENIS_PLAYER);
	}
	
	// Player ass:
	public static boolean isPlayerFreeAnus() {
		return getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER)==null;
	}
	public static boolean isPlayerAnusNoPartnerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.ANUS_PLAYER).isPlayer();
		}
	}
	
	// Player nipples:
	public static boolean isPlayerFreeNipples() {
		return getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER)==null;
	}
	public static boolean isPlayerNipplesNoPartnerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.NIPPLE_PLAYER).isPlayer();
		}
	}
	
	// Player breasts:
	public static boolean isPlayerFreeBreasts() {
		return getPenetrationTypeInOrifice(OrificeType.BREAST_PLAYER)==null;
	}
	public static boolean isPlayerBreastsNoPartnerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.BREAST_PLAYER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.BREAST_PLAYER).isPlayer();
		}
	}
	
	// Player mouth:
	public static boolean isPlayerFreeMouth() {
		return getPenetrationTypeInOrifice(OrificeType.MOUTH_PLAYER)==null && !ongoingPenetrationMap.containsKey(PenetrationType.TONGUE_PLAYER) && Main.game.getPlayer().isCoverableAreaExposed(CoverableArea.MOUTH);
	}
	public static boolean isPlayerMouthNoPartnerPenetration() {
		if(isPlayerFreeMouth()) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.MOUTH_PLAYER).isPlayer();
		}
	}
	
	// Player tail:
	public static boolean isPlayerFreeTail() {
		return !ongoingPenetrationMap.containsKey(PenetrationType.TAIL_PLAYER);
	}
	
	// Player hand:
	public static boolean hasFreeHandPlayer() {
		if(ongoingPenetrationMap.containsKey(PenetrationType.FINGER_PLAYER)) {
			return ongoingPenetrationMap.get(PenetrationType.FINGER_PLAYER).size()<2;
		} else {
			return true;
		}
		
//		return !ongoingPenetrationMap.containsKey(PenetrationType.FINGER_PLAYER);
	}

	
	// Partner vagina:
	public static boolean isPartnerFreeVagina() {
		return getPenetrationTypeInOrifice(OrificeType.VAGINA_PARTNER)==null;
	}
	public static boolean isPartnerVaginaNoPlayerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.VAGINA_PARTNER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.VAGINA_PARTNER).isPlayer();
		}
	}
	
	// Partner penis:
	public static boolean isPartnerFreePenis() {
		return !ongoingPenetrationMap.containsKey(PenetrationType.PENIS_PARTNER);
	}
	
	// Partner ass:
	public static boolean isPartnerFreeAnus() {
		return getPenetrationTypeInOrifice(OrificeType.ANUS_PARTNER)==null;
	}
	public static boolean isPartnerAnusNoPlayerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.ANUS_PARTNER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.ANUS_PARTNER).isPlayer();
		}
	}
	
	// Partner nipples:
	public static boolean isPartnerFreeNipples() {
		return getPenetrationTypeInOrifice(OrificeType.NIPPLE_PARTNER)==null;
	}
	public static boolean isPartnerNipplesNoPlayerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.NIPPLE_PARTNER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.NIPPLE_PARTNER).isPlayer();
		}
	}
	
	// Partner breasts:
	public static boolean isPartnerFreeBreasts() {
		return getPenetrationTypeInOrifice(OrificeType.BREAST_PARTNER)==null;
	}
	public static boolean isPartnerBreastsNoPlayerPenetration() {
		if(getPenetrationTypeInOrifice(OrificeType.BREAST_PARTNER)==null) {
			return true;
		} else {
			return getPenetrationTypeInOrifice(OrificeType.BREAST_PARTNER).isPlayer();
		}
	}
	
	// Partner mouth:
	public static boolean isPartnerFreeMouth() {
		return getPenetrationTypeInOrifice(OrificeType.MOUTH_PARTNER)==null && !ongoingPenetrationMap.containsKey(PenetrationType.TONGUE_PARTNER) && partner.isCoverableAreaExposed(CoverableArea.MOUTH);
	}
	public static boolean isPartnerMouthNoPlayerPenetration() {
		if(isPartnerFreeMouth()) {
			return true;
		} else {
			return !getPenetrationTypeInOrifice(OrificeType.MOUTH_PARTNER).isPlayer();
		}
	}
	
	// Partner tail:
	public static boolean isPartnerFreeTail() {
		return !ongoingPenetrationMap.containsKey(PenetrationType.TAIL_PARTNER);
	}
	// Partner hand:
	public static boolean hasFreeHandPartner() {
		if(ongoingPenetrationMap.containsKey(PenetrationType.FINGER_PARTNER)) {
			return ongoingPenetrationMap.get(PenetrationType.FINGER_PARTNER).size()<2;
		} else {
			return true;
		}
		
//		return !ongoingPenetrationMap.containsKey(PenetrationType.FINGER_PARTNER);
	}




	// Orgasm convenience methods:

	public static boolean isPlayerReadyToOrgasm() {
		return Main.game.getPlayer().getArousal()>=ArousalLevel.FIVE_ORGASM_IMMINENT.getMinimumValue();
	}
	public static boolean isPartnerReadyToOrgasm() {
		return partner.getArousal()>=ArousalLevel.FIVE_ORGASM_IMMINENT.getMinimumValue();
	}
	public static boolean isPlayerAbleToMutualOrgasm() {
		return ArousalLevel.getArousalLevelFromValue(Main.game.getPlayer().getArousal()).isMutualOrgasm();
	}
	public static boolean isPartnerAbleToMutualOrgasm() {
		return ArousalLevel.getArousalLevelFromValue(Main.game.getPlayer().getArousal()).isMutualOrgasm();
	}
	
	
	
	public static Map<PenetrationType, Set<OrificeType>> getOngoingPenetrationMap() {
		return ongoingPenetrationMap;
	}
	
	public static PenetrationType getPenetrationTypeInOrifice(OrificeType orifice) {
		for(Entry<PenetrationType, Set<OrificeType>> entry : ongoingPenetrationMap.entrySet()) {
			for(OrificeType ot : entry.getValue()) {
				if(ot==orifice) {
					return entry.getKey();
				}
			}
		}
		
		return null;
	}
	
	public static Map<OrificeType, Set<LubricationType>> getWetOrificeTypes() {
		return wetOrificeTypes;
	}
	
	public static Map<PenetrationType, Set<LubricationType>> getWetPenetrationTypes() {
		return wetPenetrationTypes;
	}

	public static void clearPlayerPenetrationRequests() {
		penetrationRequestsPlayer.clear();
	}

	public static void addPlayerPenetrationRequest(OrificeType orifice) {
		penetrationRequestsPlayer.add(orifice);
	}

	public static void removePlayerPenetrationRequest(OrificeType orifice) {
		penetrationRequestsPlayer.remove(orifice);
	}

	public static Set<OrificeType> getPlayerPenetrationRequests() {
		return penetrationRequestsPlayer;
	}

	public AbstractClothing getSelectedClothing() {
		return selectedClothing;
	}

	public static void setSelectedClothing(AbstractClothing selectedClothing) {
		Sex.selectedClothing = selectedClothing;
	}

	public static SexManagerInterface getSexManager() {
		return sexManager;
	}

	public static void setSexManager(SexManagerInterface sexManager) {
		for(PenetrationType pt : PenetrationType.values()) {
			Sex.getOngoingPenetrationMap().remove(pt);
		}
		Sex.sexManager = sexManager;
		Sex.sexManager.initSexActions();
		sexSB.append(
				"<p style='text-align:center;'><b>New position:</b> <b style='color:"+Colour.GENERIC_ARCANE.toWebHexString()+";'>"+Sex.sexManager.getPosition().getName()+"</b></br>"
				+"<i><b>"+Sex.sexManager.getPosition().getDescription()+"</b></i></p>");
	}

	public static List<SexActionInterface> getAvailableSexActionsPlayer() {
		return availableSexActionsPlayer;
	}

	public static SexActionInterface getLastUsedPlayerAction() {
		return lastUsedPlayerAction;
	}

	public static SexActionInterface getLastUsedPartnerAction() {
		return lastUsedPartnerAction;
	}
	
	public static boolean isPlayerDom() {
		return sexPacePlayer.isDom();
	}

	public static boolean isSexFinished() {
		return sexFinished;
	}

	public static void forceSexEnd() {
		sexFinished = true;
	}

	public static boolean isSexStarted() {
		return sexStarted;
	}

	public static void setSexStarted(boolean sexStarted) {
		Sex.sexStarted = sexStarted;
	}

	public static Set<OrificeType> getAreasCurrentlyStretchingPlayer() {
		return areasCurrentlyStretchingPlayer;
	}

	public static Set<OrificeType> getAreasCurrentlyStretchingPartner() {
		return areasCurrentlyStretchingPartner;
	}

	public static Set<OrificeType> getAreasTooLoosePlayer() {
		return areasTooLoosePlayer;
	}

	public static Set<OrificeType> getAreasTooLoosePartner() {
		return areasTooLoosePartner;
	}

	public static int getNumberOfPlayerOrgasms() {
		return numberOfPlayerOrgasms;
	}

	public static int getNumberOfPartnerOrgasms() {
		return numberOfPartnerOrgasms;
	}

	public static SexPace getSexPacePlayer() {
		return sexPacePlayer;
	}

	public static void setSexPacePlayer(SexPace sexPacePlayer) {
		Sex.sexPacePlayer = sexPacePlayer;
	}
	
	public static SexPace getSexPacePartner() {
		return sexPacePartner;
	}

	public static void setSexPacePartner(SexPace sexPacePartner) {
		Sex.sexPacePartner = sexPacePartner;
	}
	
	public static SexPosition getPosition() {
		return sexManager.getPosition();
	}

	public static boolean isPartnerAllowedToUseSelfActions() {
		return partnerAllowedToUseSelfActions;
	}

	public static void setPartnerAllowedToUseSelfActions(boolean partnerAllowedToUseSelfActions) {
		Sex.partnerAllowedToUseSelfActions = partnerAllowedToUseSelfActions;
	}

	public static boolean isPartnerCanRemoveOwnClothes() {
		return partnerCanRemoveOwnClothes;
	}

	public static void setPartnerCanRemoveOwnClothes(boolean partnerCanRemoveOwnClothes) {
		Sex.partnerCanRemoveOwnClothes = partnerCanRemoveOwnClothes;
	}

	public static boolean isPartnerCanRemovePlayersClothes() {
		return partnerCanRemovePlayersClothes;
	}

	public static void setPartnerCanRemovePlayersClothes(boolean partnerCanRemovePlayersClothes) {
		Sex.partnerCanRemovePlayersClothes = partnerCanRemovePlayersClothes;
	}
}
