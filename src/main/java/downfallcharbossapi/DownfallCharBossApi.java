package downfallcharbossapi;

import basemod.BaseMod;
import basemod.interfaces.PreRenderSubscriber;
import charbosses.bosses.AbstractCharBoss;
import charbosses.bosses.Ironclad.CharBossIronclad;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import downfall.monsters.NeowBossFinal;
import downfallcharbossapi.patches.BossRelicEnergyPatches;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@SpireInitializer
public class DownfallCharBossApi implements PreRenderSubscriber {
    public static final Logger logger = LogManager.getLogger(DownfallCharBossApi.class.getName());

    @SuppressWarnings("unused")
    public static void initialize() {
        logger.info("Initializing DownfallBossApi");
        DownfallCharBossApi downfallCharBossApi = new DownfallCharBossApi();
        BaseMod.subscribe(downfallCharBossApi);
    }

    public static final Map<String, Class<? extends AbstractCharBoss>> bossMap = new HashMap<>();
    public static final Map<String, AbstractCard.CardColor> cardColorMap = new HashMap<>();
    public static final Map<String, BossProperties> propertiesMap = new HashMap<>();
    public static final Set<AbstractCard.CardColor> excludedCardColor = new HashSet<>();
    public static final Set<String> removedBosses = new HashSet<>();
    public static final List<String> bossModifyRecords = new ArrayList<>();

    public static void registerCharBoss(String id, Class<? extends AbstractCharBoss> charBoss, AbstractCard.CardColor cardColor, BossProperties properties) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        logger.info("registerCharBoss {}, cardColor = {}, totalBossCount = {}", id, cardColor, bossMap.size() + 1);

        bossMap.put(id, charBoss);
        cardColorMap.put(id, cardColor);
        propertiesMap.put(id, properties);
        bossModifyRecords.add(String.format("[%s] registerCharBoss %s", getCallerMethod(), id));
    }

    public static void removeCharBoss(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        logger.info("removeCharBoss {}", id);

        removedBosses.add(id);
        bossModifyRecords.add(String.format("[%s] removeCharBoss %s", getCallerMethod(), id));
    }

    public static void excludeCardColorInSneckoMod(AbstractCard.CardColor cardColor) {
        excludedCardColor.add(cardColor);
    }

    public static AbstractCard.CardColor getCardTypeByBoss(AbstractCharBoss charBoss) {
        return cardColorMap.get(charBoss.id);
    }

    public static Set<String> getAllBossIds() {
        return bossMap.keySet();
    }

    public static Set<String> getRemovedBossIds() {
        return removedBosses;
    }

    public static BossPropertiesInAct getBossPropertiesInAct(String id, int act) {
        if (id == null) {
            return null;
        }

        BossProperties metadata = propertiesMap.get(id);
        if (metadata != null) {
            return metadata.bossPropertiesMap.get(act);
        }

        return null;
    }

    public static AbstractCharBoss createCharBossInstance(String id) {
        if (id == null) {
            logger.warn("Boss id is null, fallback to Ironclad.");
            return new CharBossIronclad();
        }

        Class<? extends AbstractCharBoss> cls = bossMap.get(id);
        if (cls != null) {
            try {
                return cls.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Failed to create char boss, fallback to Ironclad.", e);
            }
        } else {
            logger.warn("Boss class is null, fallback to Ironclad.");
        }

        return new CharBossIronclad();
    }

    public static boolean hasRegisteredBoss(String id) {
        if (id == null) {
            return false;
        }

        return bossMap.containsKey(id);
    }

    public static boolean isBossAvailableInColosseum(String id) {
        if (id == null) {
            return false;
        }

        BossProperties bossProperties = propertiesMap.get(id);
        return bossProperties != null && bossProperties.availableInColosseumEvent;
    }

    public static boolean isBossAvailableInAct(String id, int act) {
        return getBossPropertiesInAct(id, act) != null;
    }

    public static List<String> getBossModifyRecords() {
        return bossModifyRecords;
    }

    public static void triggerNeowBossTakeTurn(String bossId, NeowBossFinal neow) {
        if (bossId == null) {
            return;
        }

        BossProperties bossProperties = propertiesMap.get(bossId);
        if (bossProperties != null && bossProperties.neowBossTakeTurnCallback != null) {
            bossProperties.neowBossTakeTurnCallback.accept(neow);
        }
    }

    public static void triggerNeowBossGainMinionPowers(String bossId, NeowBossFinal neow, int act) {
        if (bossId == null) {
            return;
        }

        BossPropertiesInAct bossPropertiesInAct = DownfallCharBossApi.getBossPropertiesInAct(bossId, act);
        if (bossPropertiesInAct != null && bossPropertiesInAct.neowGainMinionPowersCallback != null) {
            bossPropertiesInAct.neowGainMinionPowersCallback.accept(neow);
        }
    }

    static String getCallerMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements.length >= 3 ? stackTraceElements[3].toString() : stackTraceElements[stackTraceElements.length - 1].toString();
    }

    @Override
    public void receiveCameraRender(OrthographicCamera orthographicCamera) {
        BossRelicEnergyPatches.isBossRelicHover = false;
    }
}
