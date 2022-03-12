package downfallcharbossapi.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import downfall.actions.NeowRezAction;
import downfallcharbossapi.DownfallGauntletBossApi;
import javassist.CtBehavior;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

public class InsertGauntletBossPatches {

    @SpirePatch(cls = "downfall.monsters.NeowBossSelector", method = "returnBossOptions", optional = true)
    public static class ReturnBossOptionsPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(ArrayList<String> ___bosses) {
            ___bosses.addAll(DownfallGauntletBossApi.bossMap.keySet());
            ___bosses.removeAll(DownfallGauntletBossApi.removedBosses);

            int bossCount = ___bosses.size();
            DownfallGauntletBossApi.logger.info("Patched NeowBossSelector.returnBossOptions. BossCount = {}.", bossCount);
            if (bossCount < 3) {
                throw new AssertionError("Boss count is less than 3. It's not enough for Downfall." +
                        "\nGauntlet Boss Modify Records:\n" + String.join("\n", DownfallGauntletBossApi.bossModifyRecords) +
                        "\nBoss List:\n" + String.join("\n", ___bosses.toArray(new String[0]))
                );
            }
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                // Collections.shuffle(bosses);
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Collections.class, "shuffle");
                return LineFinder.findAllInOrder(ctBehavior, finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = NeowRezAction.class, method = "rezBoss", optional = true)
    public static class NeowRezActionPatch {
        @SpirePostfixPatch()
        public static Object Postfix(Object __result, NeowRezAction __instance, Object[] __args) {
            if (__args.length != 2) {
                return __result;
            }

            String name = (String) __args[0];
            int index = (int) __args[1];

            Class<? extends AbstractMonster> bossClass = DownfallGauntletBossApi.bossMap.get(name);
            if (bossClass != null) {
                try {
                    Constructor<? extends AbstractMonster> constructor = bossClass.getConstructor(float.class, float.class);
                    int x = (int) ReflectionHacks.getCachedMethod(NeowRezAction.class, "locationSwitch", int.class).invoke(null, index);
                    return constructor.newInstance(x, -20);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    DownfallGauntletBossApi.logger.warn("Failed to create boss instance.", e);
                    throw new RuntimeException("Failed to create boss instance.", e);
                }
            }

            return __result;
        }
    }

}
