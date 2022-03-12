package downfallcharbossapi.patches;

import champ.events.Colosseum_Evil_Champ;
import charbosses.bosses.Ironclad.CharBossIronclad;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.map.DungeonMap;
import downfall.actions.NeowGainMinionPowersAction;
import downfall.actions.NeowRezAction;
import downfall.downfallMod;
import downfall.events.Colosseum_Evil;
import downfall.monsters.NeowBossFinal;
import downfallcharbossapi.BossPropertiesInAct;
import downfallcharbossapi.DownfallCharBossApi;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class InsertCharBossPatches {

    @SpirePatch(clz = downfallMod.class, method = "resetBossList")
    public static class ResetBossListPatch {
        public static void Postfix() {
            downfallMod.possEncounterList.addAll(DownfallCharBossApi.getAllBossIds());
            downfallMod.possEncounterList.removeAll(DownfallCharBossApi.getRemovedBossIds());

            int bossCount = downfallMod.possEncounterList.size();
            DownfallCharBossApi.logger.info("Patched downfallMod.resetBossList. BossCount = {}.", bossCount);
            if (bossCount < 4) {
                throw new AssertionError("Boss count is less than 4. It's not enough for Downfall." +
                        "\nChar Boss Modify Records:\n" + String.join("\n", DownfallCharBossApi.getBossModifyRecords()) +
                        "\nBoss List:\n" + String.join("\n", downfallMod.possEncounterList.toArray(new String[0]))
                        );
            }
        }
    }

    @SpirePatch(clz = downfallMod.class, method = "receiveStartAct")
    public static class SelectBossPatch {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(downfallMod.possEncounterList.getClass().getCanonicalName())
                            && m.getMethodName().equals("remove")) {
                        m.replace("{ $_ = " + SelectBossPatch.class.getCanonicalName() + ".validateBossAvailability($proceed($$)); }");
                    }
                }
            };
        }

        public static String validateBossAvailability(Object obj) {
            return validateBossAvailabilityCommon("Select boss", obj, bossId -> DownfallCharBossApi.isBossAvailableInAct(bossId, AbstractDungeon.actNum));
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "setBoss")
    public static class BossSetPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractDungeon __instance, String key) {
            int act;
            if (__instance instanceof TheBeyond) {
                act = 3;
            } else if (__instance instanceof TheCity) {
                act = 2;
            } else {
                act = 1;
            }

            BossPropertiesInAct properties = DownfallCharBossApi.getBossPropertiesInAct(key, act);
            if (properties != null) {
                DungeonMap.boss = ImageMaster.loadImage(properties.bossImage);
                DungeonMap.bossOutline = ImageMaster.loadImage(properties.bossImageOutline);
            }
        }
    }

    @SpirePatch(clz = NeowRezAction.class, method = "rezBoss")
    public static class NeowRezActionPatch {
        public static void Postfix(NeowRezAction __instance, String name) {
            if (DownfallCharBossApi.hasRegisteredBoss(name)) {
                try {
                    Field cB = __instance.getClass().getField("cB");
                    cB.set(__instance, DownfallCharBossApi.createCharBossInstance(name));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    DownfallCharBossApi.logger.warn("Failed to add char boss to NeowRezAction.rezBoss.", e);
                }
            }
        }
    }

    @SpirePatch(clz = Colosseum_Evil.class, method = "buttonEffect")
    @SpirePatch(clz = Colosseum_Evil_Champ.class, method = "buttonEffect")
    public static class ColosseumEvilRemovePossPatch {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(downfallMod.possEncounterList.getClass().getCanonicalName())
                            && m.getMethodName().equals("remove")) {
                        m.replace("{ $_ = " + ColosseumEvilRemovePossPatch.class.getCanonicalName() + ".validateBossAvailabilityForColosseum($proceed($$)); }");
                    }
                }
            };
        }

        public static String colosseumLastBossId;
        public static Object validateBossAvailabilityForColosseum(Object id) {
            return colosseumLastBossId = validateBossAvailabilityCommon("Colosseum Event", id, DownfallCharBossApi::isBossAvailableInColosseum);
        }
    }

    @SpirePatch(clz = Colosseum_Evil.class, method = "buttonEffect")
    @SpirePatch(clz = Colosseum_Evil_Champ.class, method = "buttonEffect")
    public static class ColosseumEvilPostRemovePossPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(Object __instance, int buttonPressed) {
            if (buttonPressed == 1 && DownfallCharBossApi.hasRegisteredBoss(ColosseumEvilRemovePossPatch.colosseumLastBossId)) {
                AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(ColosseumEvilRemovePossPatch.colosseumLastBossId);
                AbstractDungeon.lastCombatMetricKey = ColosseumEvilRemovePossPatch.colosseumLastBossId;
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                // AbstractDungeon.getCurrRoom().rewards.clear();
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "clear");
                return LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = NeowGainMinionPowersAction.class, method = "update")
    public static class NeowGainMinionPowersActionPatch {
        public static void Prefix(NeowGainMinionPowersAction __instance, NeowBossFinal ___owner, int ___num) {
            String bossId = ___num == 1 ? downfallMod.Act1BossFaced : (
                    ___num == 2 ? downfallMod.Act2BossFaced : downfallMod.Act3BossFaced
                    );

            DownfallCharBossApi.triggerNeowBossGainMinionPowers(bossId, ___owner, ___num);
        }
    }

    @SpirePatch(clz = NeowBossFinal.class, method = "takeTurn")
    public static class NeowBossFinalTakeTurnPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void insertTakeTurnActions(NeowBossFinal __instance) {
            DownfallCharBossApi.triggerNeowBossTakeTurn(downfallMod.Act1BossFaced, __instance);
            DownfallCharBossApi.triggerNeowBossTakeTurn(downfallMod.Act2BossFaced, __instance);
            DownfallCharBossApi.triggerNeowBossTakeTurn(downfallMod.Act3BossFaced, __instance);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                // AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new RollMoveAction(this));
                Matcher finalMatcher = new Matcher.NewExprMatcher(RollMoveAction.class);
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    private static String validateBossAvailabilityCommon(String tag, Object obj, Predicate<String> validate) {
        String bossId = (String) obj;
        ArrayList<String> bossesToAddBack = new ArrayList<>();
        ArrayList<String> possEncounterList = downfallMod.possEncounterList;

        while (DownfallCharBossApi.hasRegisteredBoss(bossId) && !validate.test(bossId)) {
            DownfallCharBossApi.logger.info("validateBossAvailabilityCommon [{}] Boss {} cannot be chosen. Search for another one.", tag, bossId);
            bossesToAddBack.add(bossId);
            bossId = getRandomBoss(possEncounterList);
        }

        possEncounterList.addAll(bossesToAddBack);
        return bossId;
    }

    private static String getRandomBoss(ArrayList<String> possEncounterList) {
        if (possEncounterList.isEmpty()) {
            DownfallCharBossApi.logger.warn("possEncounterList is exhausted. Return Ironclad.");
            return CharBossIronclad.ID;
        }

        return possEncounterList.remove(AbstractDungeon.cardRandomRng.random(possEncounterList.size() - 1));
    }
}
