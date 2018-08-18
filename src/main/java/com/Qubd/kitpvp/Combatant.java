package com.Qubd.kitpvp;


import java.math.BigDecimal;

public class Combatant {
    private int level,gold,kills,deaths,kit;
    private BigDecimal totalExp;

    public Combatant(int kit, int level, int gold, BigDecimal totalExp, int kills, int deaths) {
        this.kit = kit;
        this.level = level;
        this.gold = gold;
        this.totalExp = totalExp;
        this.kills = kills;
        this.deaths = deaths;
    }

    public int getKit() {
        return kit;
    }

    public void setKit(int kit) {
        this.kit = kit;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public BigDecimal getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(BigDecimal totalExp) {
        this.totalExp = totalExp;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
