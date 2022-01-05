package downfallcharbossapi.patches;

import charbosses.bosses.AbstractCharBoss;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import downfall.downfallMod;
import downfallcharbossapi.DownfallCharBossApi;

@SuppressWarnings("unused")
public class CharBossEasyToForgetPatches {
    @SpirePatch(clz = AbstractCharBoss.class, method = "die")
    public static class AbstractCharBossPatch {
        public static void Postfix(AbstractCharBoss charBoss) {
            if (DownfallCharBossApi.hasRegisteredBoss(charBoss.id)) {
                DownfallCharBossApi.logger.info("Char boss {} die, call downfallMod.saveBossFight.", charBoss.id);
                downfallMod.saveBossFight(charBoss.id);
            }
        }
    }
}
