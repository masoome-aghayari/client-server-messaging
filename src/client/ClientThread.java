package client;

import calculator.Calculator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class ClientThread implements Runnable {
    String operation;
    int commandCounter;
    RandomAccessFile randomAccessFile;
    int clientNumber;
    int portNumber;

    public ClientThread(String operation, int commandCounter, RandomAccessFile randomAccessFile, int clientNumber, int portNumber) {
        this.randomAccessFile = randomAccessFile;
        this.operation = operation;
        this.commandCounter = commandCounter;
        this.clientNumber = clientNumber;
        this.portNumber = portNumber;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket("localhost", portNumber);
             Scanner scanner = new Scanner(socket.getInputStream());

             Formatter socketOut = new Formatter(socket.getOutputStream())) {

            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                if (!Objects.equals(command, "ok")) {
                    List<String> operationContent = getOperationContent(command);
                    if (validateOperation(operationContent)) {
                        String operator = operationContent.get(0);
                        double operand1 = Double.parseDouble(operationContent.get(1));
                        double operand2 = Double.parseDouble(operationContent.get(2));
                        double operationResult = resultCalculator(operator, operand1, operand2);
                        Logger resultLogger = Logger.getLogger("result");
                        resultLogger.info("command number " + operationContent.get(3) +
                                ", with commandLine number: " + operationContent.get(4) +
                                ", Processed By client" + clientNumber +
                                ", The result was: " + operationResult);
                        socketOut.format("result.out\n");
                        socketOut.flush();
                        System.out.println("command Number " + operationContent.get(3) + " done");
                    } else {
                        System.out.println("failed command: " + operationContent.toString());
                        Logger exceptionLogger = Logger.getLogger("exceptions");
                        exceptionLogger.info("Incorrect command: " + operationContent.toString());
                        socketOut.format("exceptions.out\n");
                        socketOut.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double resultCalculator(String operator, double operand1, double operand2) {
        double result = 0;
        switch (operator) {
            case "+":
                result = operand1 + operand2;
                break;
            case "-":
                result = operand1 - operand2;
                break;
            case "*":
                result = operand1 * operand2;
                break;
            case "/":
                result = operand1 / operand2;
                break;
        }
        return result;
    }

    public static boolean validateOperation(List<String> operation) {
        if (operation.size() != 5)
            return false;
        else {
            String operator = operation.get(0);
            String operand1 = operation.get(1);
            String operand2 = operation.get(2);

            if (Objects.equals(operator, "/") && operand2.matches("[-+]?([0]*\\.[0]*|[0])"))
                return false;
            else
                return List.of("+", "-", "*", "/").contains(operator) &&
                        operand1.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)") &&
                        operand2.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)");
        }
    }

    public static List<String> getOperationContent(String operationString) {
        return Arrays.asList(operationString.split(" "));
    }
}