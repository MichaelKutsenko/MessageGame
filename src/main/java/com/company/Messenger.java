package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <h1>Send and receive 10 messages to other player!</h1>
 *
 * Class works as common node for communication of two players.
 * It creates two players, waits  until both is not ready and provides communication by receiving message from one player
 * and sending to another and vise a versa.
 *
 * {@link #message} - current message.
 * {@value #CRITICAL_NUMBER_OF_MESSAGES} - max number of messages the first player can receive.
 *
 * Created by Mocart on 17-May-17.
 */
public class Messenger {
    public static final int CRITICAL_NUMBER_OF_MESSAGES = 10;
    private String message;

    public static void main(String[] args) throws InterruptedException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Messenger messenger = new Messenger();

        //Players creation
        Player player1 = new Player(reader, PlayerIndex.FIRST, messenger);
        Player player2 = new Player(reader, PlayerIndex.SECOND, messenger);

        //Waiting for players ready condition
        synchronized (player1) {
            player1.start();
            player1.wait();
        }
        synchronized (player2) {
            player2.start();
            player2.wait();
        }

        //Messages sending
        while (PlayerState.READY == player1.getPlayerState() && PlayerState.READY == player2.getPlayerState()) {
            synchronized (player1) {
                player1.notify();
                player1.wait();
            }

            if (player2.getMessageCounter() == CRITICAL_NUMBER_OF_MESSAGES){
                player2.setPlayerState(PlayerState.NOT_READY);
            }

            synchronized (player2) {
                player2.notify();
                player2.wait();
            }
        }

        player1.join();
        player2.join();

        reader.close();

        System.out.println("=================");
        System.out.println("FINISH");
    }

    /**
     * getters & setters
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
