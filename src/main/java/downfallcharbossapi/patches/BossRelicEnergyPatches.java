package downfallcharbossapi.patches;

import basemod.BaseMod;
import charbosses.bosses.AbstractCharBoss;
import charbosses.relics.AbstractCharbossRelic;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import downfallcharbossapi.DownfallCharBossApi;
import javassist.CannotCompileException;
import javassist.CtBehavior;

@SuppressWarnings("unused")
public class BossRelicEnergyPatches {
    public static boolean isBossRelicHover = false;

    @SpirePatch(clz = AbstractCharbossRelic.class, method = "render")
    public static class BossRelicRenderPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert() {
            BossRelicEnergyPatches.isBossRelicHover = true;
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCharbossRelic.class, "renderTip");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = FontHelper.class, method = "identifyOrb")
    public static class FontHelperIdentifyOrbPatch {
        @SpirePrefixPatch
        public static SpireReturn<TextureAtlas.AtlasRegion> Prefix(String word) {
            if (word.equals("[E]") && isBossRelicHover && AbstractCharBoss.boss != null) {
                TextureAtlas.AtlasRegion orb = overwriteEnergyOrb();
                if (orb != null) {
                    return SpireReturn.Return(orb);
                }
            }

            return SpireReturn.Continue();
        }
    }

    public static TextureAtlas.AtlasRegion overwriteEnergyOrb() {
        AbstractCharBoss boss = AbstractCharBoss.boss;
        switch (boss.chosenClass) {
            case IRONCLAD:
                return AbstractCard.orb_red;
            case THE_SILENT:
                return AbstractCard.orb_green;
            case DEFECT:
                return AbstractCard.orb_blue;
            case WATCHER:
                return AbstractCard.orb_purple;
            default:
                AbstractCard.CardColor cardColor = DownfallCharBossApi.getCardTypeByBoss(boss);
                switch (cardColor) {
                    case RED:
                        return AbstractCard.orb_red;
                    case GREEN:
                        return AbstractCard.orb_green;
                    case BLUE:
                        return AbstractCard.orb_blue;
                    case PURPLE:
                        return AbstractCard.orb_purple;
                    default:
                        if (cardColor != null) {
                            return BaseMod.getCardEnergyOrbAtlasRegion(cardColor);
                        }
                }
        }

        return null;
    }
}
