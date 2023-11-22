package com.example.bashe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlin.random.Random

//Model

interface DeckInterface {
    var deckCount: Int
}

class Deck(override var deckCount: Int) : DeckInterface

interface Playable {
    fun takeFromDeck(deck: DeckInterface, count: Int)
}

class Player(val playerId: Int) : Playable {
    override fun takeFromDeck(deck: DeckInterface, count: Int) {
        deck.deckCount = deck.deckCount - count
    }
}

class Bot : Playable {
    override fun takeFromDeck(deck: DeckInterface, count: Int)  {
        deck.deckCount = deck.deckCount - count
    }
}

interface Generatable<T> {
    fun generate() : T
}

class IntGenerator(private val ceil: Int) : Generatable<Int> {
    override fun generate() : Int {
        return Random.nextInt(1, ceil)
    }
}

interface EntityValidationInterface<T> {
    fun validate(entity: T?) : Boolean
}

class ItemsTakenValidator : EntityValidationInterface<Int> {
    override fun validate(entity: Int?) : Boolean =
        entity != null && entity in 1..3
}

interface GameInterface {
    fun isDeckEnded() : Boolean
}

class Game(val deck: DeckInterface, val bot: Playable, val player: Playable) : GameInterface{
    override fun isDeckEnded() : Boolean = deck.deckCount == 0
}

/////

class MainActivity : AppCompatActivity() {
    private val deck = Deck(15)
    private val player = Player(1)
    private val bot = Bot()
    private val intGenerator = IntGenerator(4)
    private val game = Game(deck, bot, player)
    private val itemsTakenValidator = ItemsTakenValidator()

    private val deckTextView by lazy {
        findViewById<TextView>(R.id.deckTextView)
    }

    private val moveButton by lazy {
        findViewById<Button>(R.id.nextPlayerButton)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val deckTextViewValue = deckTextView.text.toString()
        val buttonState = moveButton.isEnabled
        outState.putString("deckTextViewValue", deckTextViewValue)
        outState.putBoolean("moveButtonState", buttonState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemsEditText: EditText = findViewById(R.id.itemsEditText)
        val nextPlayerButton: Button = findViewById(R.id.nextPlayerButton)

        if (savedInstanceState != null){
            deckTextView.text = savedInstanceState.getString("deckTextViewValue")
            moveButton.isEnabled = savedInstanceState.getBoolean("moveButtonState")
        }
        else {
            deckTextView.text = resources.getString(R.string.items_in_deck, game.deck.deckCount)
        }

        nextPlayerButton.setOnClickListener {
            val itemsTaken = itemsEditText.text.toString().toIntOrNull()
            if (itemsTakenValidator.validate(itemsTaken) && itemsTaken!! <= game.deck.deckCount) {
                game.player.takeFromDeck(game.deck, itemsTaken)
                deckTextView.text = resources.getString(R.string.items_in_deck, game.deck.deckCount)
                itemsEditText.text.clear()

                if (game.isDeckEnded()) {
                    deckTextView.text = getString(R.string.player_win)
                    nextPlayerButton.isEnabled = false
                }
                else {
                    val itemsTakenByBot: Int
                    while (true) {
                        var value = intGenerator.generate()
                        if (value <= game.deck.deckCount) {
                            itemsTakenByBot = value
                            break
                        }
                    }
                    game.bot.takeFromDeck(game.deck, itemsTakenByBot)
                    deckTextView.text = resources.getString(R.string.items_in_deck_after_bot, game.deck.deckCount)
                    if (game.isDeckEnded()) {
                        deckTextView.text = getString(R.string.bot_win)
                        nextPlayerButton.isEnabled = false
                    }
                }
            }
            else {
                itemsEditText.error = getString(R.string.wrong_move_error)
            }
        }
    }
}