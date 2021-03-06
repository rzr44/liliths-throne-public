package com.lilithsthrone.game.sex.sexActions.dominion.cultist;

import com.lilithsthrone.game.character.attributes.CorruptionLevel;
import com.lilithsthrone.game.character.npc.dominion.Cultist;
import com.lilithsthrone.game.dialogue.utils.UtilText;
import com.lilithsthrone.game.sex.ArousalIncrease;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.game.sex.SexFlags;
import com.lilithsthrone.game.sex.SexPosition;
import com.lilithsthrone.game.sex.managers.dominion.cultist.SMDomSex;
import com.lilithsthrone.game.sex.managers.dominion.cultist.SMDomSexOral;
import com.lilithsthrone.game.sex.managers.dominion.cultist.SMSubMissionary;
import com.lilithsthrone.game.sex.managers.dominion.cultist.SMSubMissionaryOral;
import com.lilithsthrone.game.sex.managers.dominion.cultist.SMSubSealed;
import com.lilithsthrone.game.sex.managers.dominion.cultist.SMSubSealedOral;
import com.lilithsthrone.game.sex.sexActions.SexAction;
import com.lilithsthrone.game.sex.sexActions.SexActionType;

/**
 * @since 0.1.88
 * @version 0.1.88
 * @author Innoxia
 */
public class SASpecialCultist {

	public static final SexAction PARTNER_SEALED = new SexAction(
			SexActionType.PARTNER,
			ArousalIncrease.ZERO_NONE,
			ArousalIncrease.ZERO_NONE,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "";
		}

		@Override
		public String getActionDescription() {
			return "";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return Sex.isPlayerDom() && ((Cultist)Sex.getPartner()).isSealedSex();
		}

		@Override
		public String getDescription() {
			return UtilText.returnStringAtRandom(
					"[npc.Name] tries to make a move, but the Witch's Seal is too strong, and [npc.she] collapses back onto the altar, stunned.",
					"The purple glow of a pentagram materialises beneath [npc.name]'s body as [npc.she] tries to make a move; proof that the Witch's Seal is still keeping [npc.herHim] bound in place.",
					"[npc.Name] tries and sit up on the altar, but [npc.she]'s only able to squirm about a little under the immobilising effects of the Witch's Seal.",
					"The soft purple glow of the Witch's Seal can be seen all around [npc.name] as [npc.she] struggles to make a move.",
					"[npc.speech(~Mmm!~)] [npc.name] moans, struggling in vain against the Witch's Seal.",
					"[npc.speech(~Aah!~)] [npc.name] whimpers, squirming about on the altar as the With's Seal keeps her locked in place.");
		}
	};
	
	public static final SexAction PLAYER_SEALED = new SexAction(
			SexActionType.PLAYER,
			ArousalIncrease.ZERO_NONE,
			ArousalIncrease.ZERO_NONE,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {
		@Override
		public String getActionTitle() {
			return "[style.boldArcane(Sealed!)]";
		}

		@Override
		public String getActionDescription() {
			return "The Witch's Seal that [npc.name] cast on you is preventing you from making a move!";
		}

		@Override
		public boolean isBaseRequirementsMet() {
			return !Sex.isPlayerDom() && ((Cultist)Sex.getPartner()).isSealedSex();
		}

		@Override
		public String getDescription() {
			return UtilText.returnStringAtRandom(
					"You try to make a move, but the Witch's Seal is too strong, and you soon find yourself forced back onto the altar, stunned by the powerful spell.",
					"The purple glow of a pentagram materialises beneath your body as you try to make a move. An invisible arcane force quickly forces you back down onto the altar; proof that the Witch's Seal is still in effect.",
					"You try to sit up on the altar, but you end up only being able to squirm about a little under the immobilising effects of the Witch's Seal.",
					"The soft purple glow of the Witch's Seal can be seen all around your body as you struggle to make a move.",
					"[pc.speech(~Mmm!~)] you [pc.moan], struggling in vain against the Witch's Seal.",
					"[pc.speech(~Aah!~)] you whimper, squirming about on the altar as the With's Seal keeps you locked in place.");
		}
	};
	
	public static final SexAction PLAYER_FORCE_POSITION_MISSIONARY = new SexAction(
			SexActionType.PLAYER_POSITIONING,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {

		@Override
		public boolean isBaseRequirementsMet() {
			return !SexFlags.positioningBlockedPlayer
					&& Sex.getPosition() != SexPosition.CULTIST_ALTAR_MISSIONARY_DOM
					&& Sex.isPlayerDom();
		}
		
		@Override
		public String getActionTitle() {
			return "Missionary";
		}

		@Override
		public String getActionDescription() {
			return "Stand up so that you're positioned between [npc.name]'s [npc.legs].";
		}

		@Override
		public String getDescription() {
			return "Having had enough of pleasuring [npc.name] with your mouth, you stand up and step forwards, bringing your groin up against [npc.hers]."
					+ " Grabbing hold of [npc.her] [npc.legs+], you push them apart a little more and [pc.moan], "
					+ "[pc.speech(Time to have some real fun!)]";
		}

		@Override
		public void applyEffects() {
			Sex.setSexManager(new SMDomSex());
			
			SexFlags.positioningBlockedPartner = true;
			SexFlags.resetRequests();
		}
	};
	
	public static final SexAction PLAYER_FORCE_POSITION_MISSIONARY_ORAL = new SexAction(
			SexActionType.PLAYER_POSITIONING,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {

		@Override
		public boolean isBaseRequirementsMet() {
			return !SexFlags.positioningBlockedPlayer
					&& Sex.getPosition() != SexPosition.CULTIST_ALTAR_MISSIONARY_ORAL_DOM
					&& Sex.isPlayerDom();
		}
		
		@Override
		public String getActionTitle() {
			return "Missionary Oral";
		}

		@Override
		public String getActionDescription() {
			return "Drop down onto your knees and position your face between [npc.name]'s legs.";
		}

		@Override
		public String getDescription() {
			return "Dropping down to your knees, you position your head between [npc.name]'s [npc.legs], ready to pleasure [npc.herHim] with your mouth,"
					+ " [pc.speech(Ready for some fun?)]";
		}

		@Override
		public void applyEffects() {
			Sex.setSexManager(new SMDomSexOral());
			
			SexFlags.positioningBlockedPartner = true;
			SexFlags.resetRequests();
		}
	};
	
	public static final SexAction PARTNER_POSITION_MISSIONARY_ORAL = new SexAction(
			SexActionType.PARTNER_POSITIONING,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {

		@Override
		public boolean isBaseRequirementsMet() {
			return !SexFlags.positioningBlockedPartner
					&& Sex.getPosition() != SexPosition.CULTIST_ALTAR_MISSIONARY_ORAL
					&& Sex.getPartner().getSexPositionPreferences().contains(SexPosition.CULTIST_ALTAR_MISSIONARY_ORAL)
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getActionTitle() {
			return "Missionary Oral";
		}

		@Override
		public String getActionDescription() {
			return "Drop down onto your knees and position your face between [pc.name]'s legs.";
		}

		@Override
		public String getDescription() {
			return "Running [npc.her] [npc.hands] down your body, [npc.name] drops down onto [npc.her] knees, positioning [npc.her] head between your [pc.legs] and [npc.moaning],"
					+ " [npc.speech(Stay still and enjoy this!)]";
		}

		@Override
		public void applyEffects() {
			if(((Cultist)Sex.getPartner()).isSealedSex()) {
				Sex.setSexManager(new SMSubSealedOral());
			} else {
				Sex.setSexManager(new SMSubMissionaryOral());
			}
			
			SexFlags.positioningBlockedPlayer = true;
			SexFlags.resetRequests();
		}
	};
	
	public static final SexAction PARTNER_POSITION_MISSIONARY = new SexAction(
			SexActionType.PARTNER_POSITIONING,
			ArousalIncrease.ONE_MINIMUM,
			ArousalIncrease.ONE_MINIMUM,
			CorruptionLevel.ZERO_PURE,
			null,
			null) {

		@Override
		public boolean isBaseRequirementsMet() {
			return !SexFlags.positioningBlockedPartner
					&& Sex.getPosition() != SexPosition.CULTIST_ALTAR_MISSIONARY
					&& Sex.getPartner().getSexPositionPreferences().contains(SexPosition.CULTIST_ALTAR_MISSIONARY)
					&& !Sex.isPlayerDom();
		}

		@Override
		public String getActionTitle() {
			return "Missionary";
		}

		@Override
		public String getActionDescription() {
			return "Stand up so that you're positioned between [pc.name]'s [pc.legs].";
		}

		@Override
		public String getDescription() {
			return "Gripping your thighs with [npc.her] [npc.hands+], [npc.name] stands up and steps forward, pushing your [pc.legs] apart slightly as [npc.she] brings [npc.her] groin up against yours,"
					+ " [npc.speech(This is going to be <i>so</i> much fun!)]";
		}

		@Override
		public void applyEffects() {
			if(((Cultist)Sex.getPartner()).isSealedSex()) {
				Sex.setSexManager(new SMSubSealed());
			} else {
				Sex.setSexManager(new SMSubMissionary());
			}
			
			SexFlags.positioningBlockedPlayer = true;
			SexFlags.resetRequests();
		}
	};
}
