package com.toyknight.aeii.manager;

import static com.toyknight.aeii.rule.Rule.Entry.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.toyknight.aeii.entity.*;
import com.toyknight.aeii.record.Recorder;
import com.toyknight.aeii.rule.Analyzer;
import com.toyknight.aeii.rule.Rule;
import com.toyknight.aeii.utils.Language;
import com.toyknight.aeii.utils.UnitFactory;
import com.toyknight.aeii.utils.UnitToolkit;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author toyknight 11/1/2015.
 */
public class GameEventExecutor {

    private final GameManager game_manager;

    private final Queue<GameEvent> event_queue;

    private final Queue<GameEvent> buffer_event_queue;

    private final AnimationDispatcher animation_dispatcher;

    public GameEventExecutor(GameManager game_manager, AnimationDispatcher dispatcher) {
        this.game_manager = game_manager;
        this.animation_dispatcher = dispatcher;
        this.event_queue = new LinkedList<GameEvent>();
        this.buffer_event_queue = new LinkedList<GameEvent>();
    }

    public GameManager getGameManager() {
        return game_manager;
    }

    public GameCore getGame() {
        return getGameManager().getGame();
    }

    public UnitToolkit getUnitToolkit() {
        return getGameManager().getUnitToolkit();
    }

    public AnimationDispatcher getAnimationDispatcher() {
        return animation_dispatcher;
    }

    public void clearGameEvents() {
        event_queue.clear();
        buffer_event_queue.clear();
    }

    public boolean isProcessing() {
        return event_queue.size() > 0 || buffer_event_queue.size() > 0;
    }

    public void submitGameEvent(GameEvent event) {
        event_queue.add(event);
    }

    private void submitBufferGameEvent(GameEvent event) {
        buffer_event_queue.add(event);
    }

    public void dispatchGameEvents() {
        if (getGame().isGameOver()) {
            getGameManager().onGameEventFinished();
        } else {
            if (buffer_event_queue.size() > 0) {
                executeGameEvent(buffer_event_queue.poll(), false);
                checkEventFinishing();
            } else {
                if (event_queue.size() > 0) {
                    executeGameEvent(event_queue.poll());
                    checkEventFinishing();
                }
            }
        }
    }

    private void checkEventFinishing() {
        if (event_queue.isEmpty() && buffer_event_queue.isEmpty()) {
            getGameManager().onGameEventFinished();
        }
    }

    public void executeGameEvent(GameEvent event) {
        executeGameEvent(event, true);
    }

    public void executeGameEvent(GameEvent event, boolean record) {
        switch (event.getType()) {
            case GameEvent.ATTACK:
                int attacker_x = (Integer) event.getParameter(0);
                int attacker_y = (Integer) event.getParameter(1);
                int target_x = (Integer) event.getParameter(2);
                int target_y = (Integer) event.getParameter(3);
                int attack_damage = (Integer) event.getParameter(4);
                int counter_damage = (Integer) event.getParameter(5);
                onAttack(attacker_x, attacker_y, target_x, target_y, attack_damage, counter_damage);
                break;
            case GameEvent.BUY:
                int index = (Integer) event.getParameter(0);
                int team = (Integer) event.getParameter(1);
                target_x = (Integer) event.getParameter(2);
                target_y = (Integer) event.getParameter(3);
                onBuy(index, team, target_x, target_y);
                break;
            case GameEvent.NEXT_TURN:
                onNextTurn();
                break;
            case GameEvent.HEAL:
                int healer_x = (Integer) event.getParameter(0);
                int healer_y = (Integer) event.getParameter(1);
                target_x = (Integer) event.getParameter(2);
                target_y = (Integer) event.getParameter(3);
                int heal = (Integer) event.getParameter(4);
                onHeal(healer_x, healer_y, target_x, target_y, heal);
                break;
            case GameEvent.MOVE:
                int unit_x = (Integer) event.getParameter(0);
                int unit_y = (Integer) event.getParameter(1);
                target_x = (Integer) event.getParameter(2);
                target_y = (Integer) event.getParameter(3);
                onMove(unit_x, unit_y, target_x, target_y);
                break;
            case GameEvent.OCCUPY:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                team = (Integer) event.getParameter(2);
                onOccupy(target_x, target_y, team);
                break;
            case GameEvent.REPAIR:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                onRepair(target_x, target_y);
                break;
            case GameEvent.REVERSE:
                unit_x = (Integer) event.getParameter(0);
                unit_y = (Integer) event.getParameter(1);
                target_x = (Integer) event.getParameter(2);
                target_y = (Integer) event.getParameter(3);
                onReverse(unit_x, unit_y, target_x, target_y);
                break;
            case GameEvent.SELECT:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                onSelect(target_x, target_y);
                break;
            case GameEvent.STANDBY:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                onStandby(target_x, target_y);
                break;
            case GameEvent.SUMMON:
                int summoner_x = (Integer) event.getParameter(0);
                int summoner_y = (Integer) event.getParameter(1);
                target_x = (Integer) event.getParameter(2);
                target_y = (Integer) event.getParameter(3);
                onSummon(summoner_x, summoner_y, target_x, target_y);
                break;
            case GameEvent.HP_CHANGE:
                ObjectMap change_map = (ObjectMap) event.getParameter(0);
                onHpChange(change_map);
                break;
            case GameEvent.TILE_DESTROY:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                onTileDestroy(target_x, target_y);
                break;
            case GameEvent.UNIT_DESTROY:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                onUnitDestroy(target_x, target_y);
                break;
            case GameEvent.GAIN_EXPERIENCE:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                int experience = (Integer) event.getParameter(2);
                onUnitGainExperience(target_x, target_y, experience);
                break;
            case GameEvent.ACTION_FINISH:
                target_x = (Integer) event.getParameter(0);
                target_y = (Integer) event.getParameter(1);
                onUnitActionFinish(target_x, target_y);
                break;
            case GameEvent.CHECK_TEAM_DESTROY:
                team = (Integer) event.getParameter(0);
                onCheckTeamDestroy(team);
                break;
            default:
                //do nothing
        }
        if (record) {
            Recorder.submitGameEvent(event);
        }
    }

    private void onAttack(
            int attacker_x, int attacker_y, int target_x, int target_y, int attack_damage, int counter_damage) {
        if (canAttack(attacker_x, attacker_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
            Unit defender = getGame().getMap().getUnit(target_x, target_y);
            if (attacker != null) {
                if (defender == null) {
                    getAnimationDispatcher().submitUnitAttackAnimation(attacker, target_x, target_y);
                } else {
                    if (attack_damage >= 0) {
                        defender.changeCurrentHp(-attack_damage);
                        UnitToolkit.attachAttackStatus(attacker, defender);
                        getAnimationDispatcher().submitUnitAttackAnimation(attacker, defender, attack_damage);
                        if (defender.getCurrentHp() <= 0) {
                            getGame().getStatistics().addDestroy(attacker.getTeam(), defender.getPrice());
                            submitBufferGameEvent(new GameEvent(GameEvent.UNIT_DESTROY, target_x, target_y));
                        }
                    }
                    if (counter_damage >= 0) {
                        attacker.changeCurrentHp(-counter_damage);
                        UnitToolkit.attachAttackStatus(defender, attacker);
                        getAnimationDispatcher().submitUnitAttackAnimation(defender, attacker, counter_damage);
                        if (attacker.getCurrentHp() <= 0) {
                            getGame().getStatistics().addDestroy(defender.getTeam(), attacker.getPrice());
                            submitBufferGameEvent(new GameEvent(GameEvent.UNIT_DESTROY, attacker_x, attacker_y));
                        }
                    }
                }
            }
        }
    }

    private boolean canAttack(int attacker_x, int attacker_y, int target_x, int target_y) {
        Unit attacker = getGame().getMap().getUnit(attacker_x, attacker_y);
        Unit defender = getGame().getMap().getUnit(target_x, target_y);
        if (attacker == null) {
            return false;
        } else {
            if (defender == null) {
                Tile tile = getGame().getMap().getTile(target_x, target_y);
                return tile != null && tile.isDestroyable();
            } else {
                return getGame().isEnemy(attacker, defender);
            }
        }
    }

    private void onBuy(int index, int team, int target_x, int target_y) {
        if (canBuy(index, team)) {
            getGameManager().requestMapFocus(target_x, target_y);
            int price = getGame().getUnitPrice(index, team);
            if (index == UnitFactory.getCommanderIndex()) {
                getGame().restoreCommander(team, target_x, target_y);
            } else {
                getGame().createUnit(index, team, target_x, target_y);
            }
            getGame().getCurrentPlayer().changeGold(-price);
            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().setSelectedUnit(unit);

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().beginMovePhase();
            }
        }
    }

    private boolean canBuy(int index, int team) {
        return getGame().isPlayerAvailable(team)
                && getGame().getPlayer(team).getGold() >= getGame().getUnitPrice(index, team)
                && getGame().getPlayer(team).getPopulation() < getGame().getRule().getInteger(MAX_POPULATION);
    }

    private void onNextTurn() {
        getGameManager().setState(GameManager.STATE_SELECT);
        getGame().nextTurn();
        int team = getGame().getCurrentTeam();
        int income = getGame().gainIncome(team);
        getAnimationDispatcher().submitMessageAnimation(
                Language.getText("LB_CURRENT_TURN") + ": " + getGame().getCurrentTurn(),
                Language.getText("LB_INCOME") + ": " + income,
                0.8f);

        //update status
        Array<Point> unit_position_set = getGame().getMap().getUnitPositionSet().toArray();
        for (Point position : unit_position_set) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            if (unit.getTeam() == team) {
                unit.updateStatus();
                getGame().resetUnit(unit);
            }
        }

        //calculate hp change at turn start
        ObjectMap<Point, Integer> hp_change_map = new ObjectMap<Point, Integer>();

        //terrain heal and poison damage
        for (Point position : getGame().getMap().getUnitPositionSet()) {
            Unit unit = getGame().getMap().getUnit(position.x, position.y);
            int change = 0;
            if (unit.getTeam() == team) {
                //the terrain heal
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                change = getUnitToolkit().getTerrainHeal(unit, tile);
                //the poison damage
                if (unit.hasStatus(Status.POISONED)) {
                    if (unit.hasAbility(Ability.UNDEAD)) {
                        change += Rule.POISON_DAMAGE;
                    } else {
                        change = -Rule.POISON_DAMAGE;
                    }
                }
                //rehabilitation
                if (unit.hasAbility(Ability.REHABILITATION)) {
                    change += unit.getMaxHp() / 4;
                }
            } else {
                Tile tile = getGame().getMap().getTile(unit.getX(), unit.getY());
                if (getGame().isEnemy(unit.getTeam(), team) && tile.isCastle() && tile.getTeam() == team) {
                    change = -50;
                }
            }
            change = UnitToolkit.validateHpChange(unit, change);
            if (change != 0) {
                hp_change_map.put(position, change);
            }
        }
        submitBufferGameEvent(new GameEvent(GameEvent.HP_CHANGE, hp_change_map));
    }

    private void onHeal(int healer_x, int healer_y, int target_x, int target_y, int heal) {
        if (canHeal(healer_x, healer_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit target = getGame().getMap().getUnit(target_x, target_y);
            target.changeCurrentHp(heal);
            getAnimationDispatcher().submitHpChangeAnimation(target, heal);
            if (target.getCurrentHp() <= 0) {
                submitBufferGameEvent(new GameEvent(GameEvent.UNIT_DESTROY, target_x, target_y));
            }
        }
    }

    private boolean canHeal(int healer_x, int healer_y, int target_x, int target_y) {
        return getGame().getMap().getUnit(healer_x, healer_y) != null
                && getGame().getMap().getUnit(target_x, target_y) != null;
    }

    private void onMove(int unit_x, int unit_y, int target_x, int target_y) {
        if (canMove(unit_x, unit_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
            getGameManager().setSelectedUnit(unit);
            getGameManager().createMovablePositions();
            Array<Point> move_path = getGameManager().getMovePath(target_x, target_y);
            int movement_point = getGameManager().getMovementPointRemains(target_x, target_y);

            getGame().moveUnit(unit_x, unit_y, target_x, target_y);
            unit.setCurrentMovementPoint(movement_point);
            getAnimationDispatcher().submitUnitMoveAnimation(unit, move_path);
            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().onUnitMoveFinish();
            }
        }
    }

    private boolean canMove(int unit_x, int unit_y, int target_x, int target_y) {
        Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
        return unit != null && getGame().canUnitMove(unit, target_x, target_y);
    }

    private void onOccupy(int target_x, int target_y, int team) {
        if (canOccupy(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getCapturedTileIndex(team), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_OCCUPIED"), 0.5f);

            submitBufferGameEvent(new GameEvent(GameEvent.CHECK_TEAM_DESTROY, target_tile.getTeam()));
        }
    }

    private boolean canOccupy(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isCapturable();
    }

    private void onRepair(int target_x, int target_y) {
        if (canRepair(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Tile target_tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(target_tile.getRepairedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitMessageAnimation(Language.getText("LB_REPAIRED"), 0.5f);

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                Unit unit = getGame().getMap().getUnit(target_x, target_y);
                getGameManager().onUnitActionFinish(unit);
            }
        }
    }

    private boolean canRepair(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isRepairable();
    }

    private void onReverse(int unit_x, int unit_y, int origin_x, int origin_y) {
        if (canReverse(unit_x, unit_y)) {
            getGameManager().requestMapFocus(origin_x, origin_y);

            Unit unit = getGame().getMap().getUnit(unit_x, unit_y);
            if (getGame().getMap().canMove(origin_x, origin_y)) {
                getGame().getMap().moveUnit(unit, origin_x, origin_y);
            }
            unit.setCurrentMovementPoint(unit.getMovementPoint());

            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                getGameManager().beginMovePhase();
            }
        }
    }

    private boolean canReverse(int unit_x, int unit_y) {
        return getGame().getMap().getUnit(unit_x, unit_y) != null;
    }

    private void onSelect(int target_x, int target_y) {
        if (canSelect(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().setSelectedUnit(unit);
            if (getGame().getCurrentPlayer().isLocalPlayer()) {
                Unit selected_unit = getGameManager().getSelectedUnit();
                Tile tile = getGame().getMap().getTile(target_x, target_y);
                if (selected_unit.isCommander() && !selected_unit.isStandby() && getGame().isCastleAccessible(tile)) {
                    getGameManager().setState(GameManager.STATE_BUY);
                } else {
                    if (!selected_unit.isStandby()) {
                        getGameManager().beginMovePhase();
                    }
                }
            }
        }
    }

    private boolean canSelect(int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return target != null && getGame().getCurrentTeam() == target.getTeam() && !target.isStandby();
    }

    private void onStandby(int target_x, int target_y) {
        if (canStandby(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGame().standbyUnit(target_x, target_y);
            getGameManager().setState(GameManager.STATE_SELECT);

            ObjectSet<Point> aura_positions = getGameManager().createPositionsWithinRange(target_x, target_y, 0, 2);

            //all the status auras
            for (Point target_position : aura_positions) {
                Unit target = getGame().getMap().getUnit(target_position.x, target_position.y);
                if (target != null) {
                    if (unit.hasAbility(Ability.ATTACK_AURA) && !getGame().isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.INSPIRED, 0));
                    }
                    if (unit.hasAbility(Ability.SLOWING_AURA)
                            && !target.hasAbility(Ability.SLOWING_AURA) && getGame().isEnemy(unit, target)) {
                        target.attachStatus(new Status(Status.SLOWED, 1));
                    }
                }
            }
            //the refresh aura
            ObjectMap<Point, Integer> hp_change_map = new ObjectMap<Point, Integer>();
            if (unit.hasAbility(Ability.REFRESH_AURA)) {
                int heal = Rule.REFRESH_BASE_HEAL + unit.getLevel() * 5;
                for (Point target_position : aura_positions) {
                    Unit target = getGame().getMap().getUnit(target_position.x, target_position.y);
                    if (getGame().canClean(unit, target)) {
                        target.clearStatus();
                    }
                    if (getGame().canHeal(unit, target)) {
                        int change = UnitToolkit.validateHpChange(target, heal);
                        if (change != 0) {
                            hp_change_map.put(target_position, change);
                        }
                    }
                }
            }
            //deal with tombs
            if (getGame().getMap().isTomb(unit.getX(), unit.getY())) {
                getGame().getMap().removeTomb(unit.getX(), unit.getY());
                if (!unit.hasAbility(Ability.NECROMANCER)) {
                    unit.attachStatus(new Status(Status.POISONED, 3));
                }
            }
            submitBufferGameEvent(new GameEvent(GameEvent.HP_CHANGE, hp_change_map));
        }
    }

    private boolean canStandby(int target_x, int target_y) {
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return target != null && !target.isStandby() && target.getCurrentHp() > 0;
    }

    private void onSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        if (canSummon(summoner_x, summoner_y, target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit summoner = getGame().getMap().getUnit(summoner_x, summoner_y);
            getGame().getMap().removeTomb(target_x, target_y);
            getGame().createUnit(UnitFactory.getSkeletonIndex(), summoner.getTeam(), target_x, target_y);
            getAnimationDispatcher().submitSummonAnimation(summoner, target_x, target_y);
        }
    }

    private boolean canSummon(int summoner_x, int summoner_y, int target_x, int target_y) {
        Unit summoner = getGame().getMap().getUnit(summoner_x, summoner_y);
        Unit target = getGame().getMap().getUnit(target_x, target_y);
        return summoner != null && target == null && getGame().getMap().isTomb(target_x, target_y);
    }

    private void onTileDestroy(int target_x, int target_y) {
        if (canDestroyTile(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Tile tile = getGame().getMap().getTile(target_x, target_y);
            getGame().setTile(tile.getDestroyedTileIndex(), target_x, target_y);
            getAnimationDispatcher().submitDustAriseAnimation(target_x, target_y);
        }
    }

    private boolean canDestroyTile(int target_x, int target_y) {
        Tile tile = getGame().getMap().getTile(target_x, target_y);
        return tile != null && tile.isDestroyable();
    }

    private void onUnitDestroy(int target_x, int target_y) {
        if (canDestroyUnit(target_x, target_y)) {
            getGameManager().requestMapFocus(target_x, target_y);

            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGame().destroyUnit(unit.getX(), unit.getY());
            getAnimationDispatcher().submitUnitDestroyAnimation(unit);
            getAnimationDispatcher().submitDustAriseAnimation(unit.getX(), unit.getY());

            submitBufferGameEvent(new GameEvent(GameEvent.CHECK_TEAM_DESTROY, unit.getTeam()));
        }
    }

    private boolean canDestroyUnit(int target_x, int target_y) {
        return getGame().getMap().getUnit(target_x, target_y) != null;
    }

    private void onUnitGainExperience(int target_x, int target_y, int experience) {
        Unit unit = getGame().getMap().getUnit(target_x, target_y);
        if (unit != null) {
            boolean level_up = unit.gainExperience(experience);
            if (level_up) {
                getAnimationDispatcher().submitUnitLevelUpAnimation(unit);
            }
        }
    }

    private void onUnitActionFinish(int target_x, int target_y) {
        if (getGame().getCurrentPlayer().isLocalPlayer()) {
            Unit unit = getGame().getMap().getUnit(target_x, target_y);
            getGameManager().onUnitActionFinish(unit);
        }
    }

    private void onHpChange(ObjectMap<Point, Integer> change_map) {
        if (change_map.keys().toArray().size > 0) {
            ObjectSet<Unit> units = new ObjectSet<Unit>();
            for (Point position : change_map.keys()) {
                Unit target = getGame().getMap().getUnit(position);
                target.changeCurrentHp(change_map.get(position));
                units.add(target);
                if (target.getCurrentHp() <= 0) {
                    submitBufferGameEvent(new GameEvent(GameEvent.UNIT_DESTROY, target.getX(), target.getY()));
                }
            }
            getAnimationDispatcher().submitHpChangeAnimation(change_map, units);
        }
    }

    private void onCheckTeamDestroy(int team) {
        Analyzer analyzer = new Analyzer(getGame());
        if (analyzer.isTeamDestroyed(team)) {
            getGame().removeTeam(team);
            int winner_alliance = analyzer.getWinnerAlliance();
            if (winner_alliance >= 0) {
                getGame().setGameOver(true);
            }
        }
    }

}
