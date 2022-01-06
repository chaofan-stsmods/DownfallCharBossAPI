# 崩坠首领API

[English](README.md)

## 简介

这个API能让你给崩坠加入自己的角色作为首领出现。

## 怎么用

1.  在Steam创意工坊订阅[这个mod](https://steamcommunity.com/sharedfiles/filedetails/?id=2708878699)。

2.  将以下依赖加入你的`pom.xml`（假设你已经把崩坠加为了依赖）：

    ```xml
    <dependency>
        <groupId>downfallcharbossapi</groupId>
        <artifactId>downfallcharbossapi</artifactId>
        <version>1.0.0</version>
        <scope>system</scope>
        <systemPath>${Steam.path}/workshop/content/646570/2708878699/downfallcharbossapi.jar</systemPath>
    </dependency>
    ```

3.  继承崩坠的`AbstractCharBoss`类，实现你自己的角色首领。

4.  注册你的角色首领：

    ```java
    @Override
    public void receivePostInitialize() {
        DownfallCharBossApi.registerCharBoss(
                CharBossBladeGunner.ID,         // 角色首领ID
                CharBossBladeGunner.class,      // 角色首领Java类
                BladeGunnerEnums.CARD_COLOR,    // 卡片颜色，它决定了遗物说明中能量的图标。
                new BossProperties()
                        // 当NeowBossFinal.takeTurn被调用时触发，
                        // 时机是所有Action放入队列之后，涅奥设置下次行动之前。
                        // 你可以加入额外Action来控制涅奥的行动。
                        // 注意：
                        // 所有当前游戏中打败的角色首领的回调都会被调用。
                        // 先检查涅奥是否有相应能力或者Buff，再加入额外Action。
                        .setNeowBossTakeTurnCallback(CharBossBladeGunner::neowBossTakeTurn)
    
                        // 你的角色首领是否会出现在斗兽场事件中。
                        .setAvailableInColosseumEvent(true)
    
                        // 第1~3章中你的角色首领属性。
                        // 如果你没有设置某章的属性，那么你的角色首领就不会出现在那章中。
                        .setPropertiesInAct(1, new BossPropertiesInAct(
                                // 地图上的首领图片
                                "bladegunnerdownfall/ui/bladegunneract1.png",
    
                                // 首领图片的边框
                                "bladegunnerdownfall/ui/bladegunneract1outline.png",
    
                                // 当你在最终战打涅奥前，她会从你打过的首领中得到能力，此时触发这个回调。
                                // 将你的角色首领的能力给予涅奥。
                                neow -> AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(neow, neow, new NeowRepreparaionPower(neow)))))
    
                        // 第2~3章和第1章类似
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
    
        // 记得注册你的角色首领到BaseMod。
        // 保证这里的ID一定是首领ID。
        BaseMod.addMonster(CharBossBladeGunner.ID, () -> new CharBossMonsterGroup(new AbstractMonster[] { new CharBossBladeGunner() }));
    }
    ```

## 其他API

*   `removeCharBoss`

    移除角色首领。它不再会出现在战斗中。你也可以移除原版崩坠的Boss。要注意崩坠至少需要4个首领（3章 + 斗兽场事件1个）。
    如果你开始游戏时首领不足4个，会抛出异常并使游戏闪退。
    
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

    你可能会给你的角色首领注册独立的卡牌颜色。异蛇会给每种卡牌颜色生成一张角色随机卡，在你这里就会总是“疯狂”。
    你可以用这个API去防止异蛇给这种卡牌颜色生成角色随机卡。
    
    ```java
    @SpireInitializer
    public class YourMod {
        public static void initialize() {
            DownfallCharBossApi.excludeCardColorInSneckoMod(YourEnums.YOUR_CARD_COLOR);
        }
    }
    ```