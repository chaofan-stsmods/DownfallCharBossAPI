package downfallcharbossapi;

import downfall.monsters.NeowBossFinal;

import java.util.function.Consumer;

public class BossPropertiesInAct {
    public final String bossImage;
    public final String bossImageOutline;
    public final Consumer<NeowBossFinal> neowGainMinionPowersCallback;

    public BossPropertiesInAct(
            String bossImage,
            String bossImageOutline,
            Consumer<NeowBossFinal> neowGainMinionPowersCallback
    ) {
        this.bossImage = bossImage;
        this.bossImageOutline = bossImageOutline;
        this.neowGainMinionPowersCallback = neowGainMinionPowersCallback;
    }
}
