package org.circularBuffer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args)
    {
        Scanner sc = new Scanner(System.in);
        System.out.println("Pass the capacity of the buffer: ");
        int capacity = sc.nextInt();

        CircularBuffer cb = new CircularBuffer(capacity);

        boolean run=true;
        while(run)
        {
            System.out.println("/f");
            System.out.println("Select the option: 1/2/3");
            System.out.println("1. Read");
            System.out.println("2. Write");
            System.out.println("3. Terminate");

            int option = sc.nextInt();
            if(option!=1 && option!=2) {
                System.out.println("Incorrect Option selected, please select the correct option");
                continue;
            }


        }
    }
}