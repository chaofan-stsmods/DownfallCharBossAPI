# Downfall Char Boss API

[中文版](README.cn.md)

## Overview

This API can help you insert your character as a boss to Downfall.

## How to use

1.  Subscribe [this mod](https://steamcommunity.com/sharedfiles/filedetails/?id=2708878699) from Steam workshop.

2.  Add following dependency to your `pom.xml` (I assume you already added Downfall as dependency):

    ```xml
    <dependency>
        <groupId>downfallcharbossapi</groupId>
        <artifactId>downfallcharbossapi</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${Steam.path}/workshop/content/646570/2708878699/downfallcharbossapi.jar</systemPath>
    </dependency>
    ```

3.  Implement your char boss, which inherits `AbstractCharBoss` class from Downfall.

4.  Register your char boss:

    ```java
    @Override
    public void receivePostInitialize() {
        DownfallCharBossApi.registerCharBoss(
                CharBossBladeGunner.ID,         // Char Boss ID
                CharBossBladeGunner.class,      // Char Boss class
                BladeGunnerEnums.CARD_COLOR,    // Card color, it controls energy orb image in tooltip of relics
                new BossProperties()
                        // This is triggered when NeowBossFinal.takeTurn is called,
                        // after all Actions are queued, before Neow set next move.
                        // You can add additional Actions when Neow takes turn.
                        // NOTICE:
                        // Callback of ALL bosses killed in current game will be called.
                        // Check whether Neow has your power before adding Actions.
                        .setNeowBossTakeTurnCallback(CharBossBladeGunner::neowBossTakeTurn)
    
                        // Whether your char boss can be chosen in Event "Colosseum".
                        .setAvailableInColosseumEvent(true)
    
                        // Properties of your char boss in act 1~3
                        // If you don't set properties of an act, the char boss won't be chosen as boss of that act.
                        .setPropertiesInAct(1, new BossPropertiesInAct(
                                // Boss image shown in map
                                "bladegunnerdownfall/ui/bladegunneract1.png",
    
                                // Boss image outline
                                "bladegunnerdownfall/ui/bladegunneract1outline.png",
    
                                // This is triggered when starting final battle and Neow is getting power from char bosses you met.
                                // Apply your power to Neow in this callback.
                                neow -> AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(neow, neow, new NeowRepreparaionPower(neow)))))
    
                        // Act 2~3 are similar as act 1
                        .setPropertiesInAct(2, new BossPropertiesInAct(
                                "bladegunnerdownfall/ui/bladegunneract2.png",
                                "bladegunnerdownfall/ui/bladegunneract2outline.png",
                                neow -> AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(neow, neow, new NeowCollectionAddictedPower(neow)))))
    
                        .setPropertiesInAct(3, new BossPropertiesInAct(
                                "bladegunnerdownfall/ui/bladegunneract3.png",
                                "bladegunnerdownfall/ui/bladegunneract3outline.png",
                                neow -> {
                                    AbstractPlayer player = AbstractDungeon.player;
                                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(neow, neow, new NeowGunfirePower(neow, 4)));
                                    AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(player, player, new BurningResistancePower(player)));
                                }))
        );
    
        // Don't forget to add a monster group for you char boss.
        // Make sure monster group ID is the same as boss ID.
        BaseMod.addMonster(CharBossBladeGunner.ID, () -> new CharBossMonsterGroup(new AbstractMonster[] { new CharBossBladeGunner() }));
    }
    ```

## Other APIs

*   `removeCharBoss`

    Remove a char boss. It won't appear in battles. You may remove bosses from Downfall as well. Note that Downfall
    requires at least 4 bosses (3 acts + 1 Colosseum event). An error will be thrown if you start a game with
    less than 4 bosses registered.
    
    ```java
    @SpireInitializer
    public class YourMod {
        public static void initialize() {
            DownfallCharBossApi.removeCharBoss(CharBossIronclad.ID);
            DownfallCharBossApi.removeCharBoss(YourBoss.ID);
        }
    }
    ```

*   `excludeCardColorInSneckoMod`

    You may create a card color for char boss. The card color is scanned by Snecko Mod and generate a Player-Random
    card which is always "Madness". To avoid this, you can use this API to avoid Snecko Mod generating card from the
    card color.
    
    ```java
    @SpireInitializer
    public class YourMod {
        public static void initialize() {
            DownfallCharBossApi.excludeCardColorInSneckoMod(YourEnums.YOUR_CARD_COLOR);
        }
    }
    ```