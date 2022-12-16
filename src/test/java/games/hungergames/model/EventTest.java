package games.hungergames.model;

import io.github.milobotdev.milobot.games.hungergames.HungerGames;
import org.junit.jupiter.api.*;
import io.github.milobotdev.milobot.games.hungergames.model.Event;
import io.github.milobotdev.milobot.games.hungergames.model.Player;

@Disabled
public class EventTest {

    HungerGames game;
    Player player;
    Player victim;

    @BeforeEach
    void setUp() {
        this.game = new HungerGames();

        this.player = new Player("Player", 0);
        this.victim = new Player("Victim", 0);

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @AfterEach
    void tearDown() {
        this.game = null;
        this.player = null;
        this.victim = null;

        this.game = new HungerGames();
        this.player = new Player("Player", 0);
        this.victim = new Player("Victim", 0);

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @Test
    void testBananaPeel() {
        Event bananaPeel = game.getEventByName("banana peel").orElseThrow(() -> new RuntimeException("banana peel not found"));

        player.triggerEvent(bananaPeel);

        Assertions.assertEquals(95, player.getHealth());
    }

    @Test
    public void testLandMine() {
        Event landMine = game.getEventByName("land mine").orElseThrow(() -> new RuntimeException("land mine not found"));

        player.triggerEvent(landMine);

        Assertions.assertEquals(50, player.getHealth());
    }

    @Test
    public void testCliffDrop() {
        Event cliffDrop = game.getEventByName("cliff drop").orElseThrow(() -> new RuntimeException("land mine not found"));

        player.triggerEvent(cliffDrop);

        Assertions.assertEquals(0, player.getHealth());
        Assertions.assertEquals(1, game.getAlivePlayers().size());
    }

    @Test
    public void findCapybara() {
        Event findCapybara = game.getEventByName("find capybara").orElseThrow(() -> new RuntimeException("land mine not found"));

        player.damage(20);
        player.triggerEvent(findCapybara);

        Assertions.assertEquals(85, player.getHealth());
    }

}
