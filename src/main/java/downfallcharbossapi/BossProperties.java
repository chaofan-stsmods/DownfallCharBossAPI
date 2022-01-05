package downfallcharbossapi;

import downfall.monsters.NeowBossFinal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BossProperties {

    public Map<Integer, BossPropertiesInAct> bossPropertiesMap = new HashMap<>();
    public Consumer<NeowBossFinal> neowBossTakeTurnCallback;
    public boolean availableInColosseumEvent;

    public BossProperties setPropertiesInAct(int act, BossPropertiesInAct properties) {
        bossPropertiesMap.put(act, properties);
        return this;
    }

    public BossProperties setNeowBossTakeTurnCallback(Consumer<NeowBossFinal> neowBossTakeTurnCallback) {
        this.neowBossTakeTurnCallback = neowBossTakeTurnCallback;
        return this;
    }

    public BossProperties setAvailableInColosseumEvent(boolean available) {
        this.availableInColosseumEvent = available;
        return this;
    }

}
