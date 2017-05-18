package com.company;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Class describes the player, communicates with another player through common node.
 * Class is launched as separated synchronized thread.
 *
 * {@link #playerName} - player alias(is inputted by player).
 * {@link #index} - player serial number. {@see com.company.PlayerIndex}.
 * {@link #messageCounter} - counts the quantity of sent messages by this player.
 * {@link #playerState} - indicates whether the player ready to send/receive messages. {@see com.company.PlayerState}.
 * {@link #messenger} - common communication node. {@see com.company.Messenger}.
 * {@link #reader} - stream reader for console reading.
 *
 * Created by Mocart on 17-May-17.
 */
public class Player extends Thread {
    private String playerName;
    private PlayerIndex index;
    private int messageCounter;
    private PlayerState playerState;
    private Messenger messenger;

    private BufferedReader reader;

    public Player(BufferedReader reader, PlayerIndex index, Messenger messenger) {
        this.reader = reader;
        this.index = index;
        this.playerState = PlayerState.NOT_READY;
        this.messenger = messenger;
    }

    @Override
    public void run() {
        synchronized (this) {
            System.out.println(String.format("Please, text name of the %s player and type \"ENTER\"", index.name()));

            try {
                //For personal alias inputting
                playerName = reader.readLine();

                playerState = PlayerState.READY;

                this.notify();
                this.wait();

                //Main job (message reading & sending)
                String fullMessage;
                StringBuilder messageBuilder;
                while (PlayerState.READY == playerState) {
                    fullMessage = receiveMessages();

                    //If the message is not the first - read its, else - do nothing
                    if (fullMessage != null){
                        System.out.println();
                        System.out.println("=================");
                        System.out.println(String.format("%s received:", playerName));
                        System.out.println("=================");
                        System.out.println(fullMessage);
                    }
                    else {
                        fullMessage = "";
                    }

                    if (messageCounter == Messenger.CRITICAL_NUMBER_OF_MESSAGES){
                        playerState = PlayerState.NOT_READY;

                        this.notify();
                        break;
                    }

                    System.out.println(String.format("%s, please, text your message and type \"ENTER\"", playerName));

                    //New message creating
                    messageBuilder = new StringBuilder(fullMessage);
                    messageBuilder
                            .append(playerName)
                            .append(": ")
                            .append(reader.readLine())
                            .append(" #")
                            .append(++messageCounter)
                            .append(System.lineSeparator());

                    sendMessage(messageBuilder.toString());

                    this.notify();
                    this.wait();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Thread finishing
        System.out.println(playerName + " is closed.");
    }

    /**
     * Sends message to the common communication node (Messenger)
     * @param message - remade massage (previous text plus current message plus number)
     */
    public void sendMessage(String message){
        messenger.setMessage(message);
    }

    /**
     * Receives the latest message from common communication node (Messenger)
     * @return the last message from another player
     */
    public String receiveMessages(){
        return messenger.getMessage();
    }

    /**
     * getters & setters
     */
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState state) {
        this.playerState = state;
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    public void setMessageCounter(int messageCounter) {
        this.messageCounter = messageCounter;
    }

    public void stopPlayer(){
        this.playerState = PlayerState.NOT_READY;
    }
}
