package downfallcharbossapi;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static downfallcharbossapi.DownfallCharBossApi.getCallerMethod;

public class DownfallGauntletBossApi {
    public static final Logger logger = LogManager.getLogger(DownfallGauntletBossApi.class.getName());

    public static final Map<String, Class<? extends AbstractMonster>> bossMap = new HashMap<>();
    public static final Set<String> removedBosses = new HashSet<>();
    public static final List<String> bossModifyRecords = new ArrayList<>();

    public static void registerGauntletBoss(String id, Class<? extends AbstractMonster> gauntletBoss) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        logger.info("registerGauntletBoss {}, totalBossCount = {}", id, bossMap.size() + 1);
        bossMap.put(id, gauntletBoss);
        bossModifyRecords.add(String.format("[%s] registerGauntletBoss %s", getCallerMethod(), id));
    }

    public static void removeGauntletBoss(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        logger.info("removeGauntletBoss {}", id);

        removedBosses.add(id);
        bossModifyRecords.add(String.format("[%s] removeGauntletBoss %s", getCallerMethod(), id));
    }
}
