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
        if (bossImage == null) {
            throw new IllegalArgumentException("bossImage is null");
        }
        if (bossImageOutline == null) {
            throw new IllegalArgumentException("bossImageOutline is null");
        }

        this.bossImage = bossImage;
        this.bossImageOutline = bossImageOutline;
        this.neowGainMinionPowersCallback = neowGainMinionPowersCallback;
    }
}
