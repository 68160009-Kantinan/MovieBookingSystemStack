import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Stack; // นำเข้าไลบรารี Stack เพื่อทำระบบ Undo

public class Main {

    // =====================================================================================
    // [ส่วนที่ 1: Intro & Concept] - โครงสร้างข้อมูลพื้นฐาน (Array 1D, 3D และ Stack)
    // =====================================================================================

    static String[] movies = {"Avenger: Endgame", "Inception", "Interstellar"};
    static double[] prices = {250.0, 200.0, 180.0};
    static String[] showtimes = {"13:00", "15:30", "19:00"};

    static final int ROWS = 5;
    static final int COLS = 10;

    // 3D Array เก็บผังที่นั่ง: [หนังเรื่องไหน][แถวไหน][คอลัมน์ไหน]
    static char[][][] seats = new char[movies.length][ROWS][COLS];
    static char[] rowLabels = {'A', 'B', 'C', 'D', 'E'};

    // [ไฮไลท์ใหม่] Stack สำหรับทำระบบ Undo (ยกเลิกการจองล่าสุด)
    // Stack ทำงานแบบ LIFO (Last-In, First-Out) คือข้อมูลที่ใส่เข้าไปล่าสุด จะถูกดึงออกมาก่อนเพื่อน
    // เราจะเก็บข้อมูลเป็น String เช่น "0,0,0,A1" เพื่อจำว่าเพิ่งจองพิกัดไหนไป
    static Stack<String> bookingHistory = new Stack<>();


    // =====================================================================================
    // [ส่วนที่ 2: Logic & Algorithm] - ภาพรวมระบบ, การเริ่มต้นผังที่นั่ง และเมนู
    // =====================================================================================

    public static void main(String[] args) {
        initializeSeats(); // เซ็ตให้ที่นั่งทุกช่องเป็น 'O'
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("=======================================");
        System.out.println("   Welcome to Cinema Booking System");
        System.out.println("=======================================");

        while (isRunning) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. View Movies (ดูรายชื่อหนัง)");
            System.out.println("2. View Seats (ดูผังที่นั่ง)");
            System.out.println("3. Book Ticket(s) (จองตั๋ว)");
            System.out.println("4. Undo Last Booking (ยกเลิกการจองล่าสุด)"); // เมนูใหม่สำหรับ Stack
            System.out.println("5. Exit (ออกจากระบบ)");

            int choice = getValidInput(scanner, "Please select an option (1-5): ", 1, 5);

            switch (choice) {
                case 1: displayMovies(); break;
                case 2: viewSeatsMenu(scanner); break;
                case 3: bookTicket(scanner); break;
                case 4: undoBooking(); break; // เรียกใช้ระบบ Undo
                case 5:
                    System.out.println("Thank you for using our system. Goodbye!");
                    isRunning = false;
                    break;
            }
        }
        scanner.close();
    }

    // ลอจิกการเคลียร์ผังที่นั่ง (Nested Loop 3 ชั้น เข้าถึง 3D Array)
    public static void initializeSeats() {
        for (int m = 0; m < movies.length; m++) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    seats[m][i][j] = 'O';
                }
            }
        }
    }

    // ลอจิกการโชว์รายชื่อหนัง ดึงจาก 1D Array 3 ตัว (ชื่อ, เวลา, ราคา) มาแสดงพร้อมกัน
    public static void displayMovies() {
        System.out.println("\n--- Now Showing ---");
        for (int i = 0; i < movies.length; i++) {
            System.out.printf("%d. %s | Showtime: %s | %.2f THB\n",
                    (i + 1), movies[i], showtimes[i], prices[i]);
        }
    }

    public static void viewSeatsMenu(Scanner scanner) {
        displayMovies();
        int movieChoice = getValidInput(scanner, "Select a movie to view seats (1-" + movies.length + "): ", 1, movies.length);
        displaySeats(movieChoice - 1);
    }

    // ลอจิกการวาดผังที่นั่ง
    public static void displaySeats(int movieIndex) {
        System.out.println("\n--- Screen for: " + movies[movieIndex] + " (" + showtimes[movieIndex] + ") ---");
        System.out.print("     ");

        for (int j = 1; j <= COLS; j++) {
            System.out.printf("%-4d", j);
        }
        System.out.println();

        for (int i = 0; i < ROWS; i++) {
            System.out.print(rowLabels[i] + "   ");
            for (int j = 0; j < COLS; j++) {
                if (seats[movieIndex][i][j] == 'O') {
                    System.out.print("[ ] ");
                } else {
                    System.out.print("[X] ");
                }
            }
            System.out.println();
        }
        System.out.println("Legend: '[ ]' = Available | '[X]' = Booked");
    }


    // =====================================================================================
    // [ส่วนที่ 3: Live Coding & Demo] - ระบบจองตั๋ว (Push) และ ระบบยกเลิก (Pop)
    // =====================================================================================

    public static void bookTicket(Scanner scanner) {
        System.out.println("\n--- Book Ticket(s) ---");
        displayMovies();
        int movieChoice = getValidInput(scanner, "Select a movie (1-" + movies.length + "): ", 1, movies.length);
        int movieIndex = movieChoice - 1;

        displaySeats(movieIndex);
        scanner.nextLine();

        System.out.print("\nSelect your seat(s) separated by space (e.g., A1 A2) or type '0' to cancel: ");
        String rawInput = scanner.nextLine().trim().toUpperCase();

        if (rawInput.equals("0")) {
            System.out.println("Booking cancelled. Returning to Main Menu...");
            return;
        }

        // หั่นข้อความด้วยลูกน้ำ (,) หรือ ช่องว่าง (Space) ก็ได้
        String[] requestedSeats = rawInput.split("[,\\s]+");

        int successCount = 0;
        String bookedSeatsList = "";
        String failedSeatsList = "";

        for (int s = 0; s < requestedSeats.length; s++) {
            String seatInput = requestedSeats[s];

            if (seatInput.length() >= 2) {
                char rowChar = seatInput.charAt(0);
                int rIndex = -1;

                for (int i = 0; i < rowLabels.length; i++) {
                    if (rowLabels[i] == rowChar) {
                        rIndex = i;
                        break;
                    }
                }

                if (rIndex != -1) {
                    try {
                        int colNum = Integer.parseInt(seatInput.substring(1));
                        int cIndex = colNum - 1;

                        if (colNum >= 1 && colNum <= COLS) {
                            if (seats[movieIndex][rIndex][cIndex] == 'O') {

                                seats[movieIndex][rIndex][cIndex] = 'X'; // เปลี่ยนเป็นจองแล้ว
                                bookedSeatsList += seatInput + " ";
                                successCount++;

                                // =======================================================
                                // [หัวใจของ Stack: PUSH] เมื่อจองสำเร็จ ให้บันทึกประวัติ
                                // เก็บรูปแบบเป็น String: "เลขหนัง,เลขแถว,เลขคอลัมน์,ชื่อที่นั่ง"
                                // เช่น "0,0,0,A1" แล้วโยน (Push) ลงไปเก็บบนสุดของ Stack
                                // =======================================================
                                String historyRecord = movieIndex + "," + rIndex + "," + cIndex + "," + seatInput;
                                bookingHistory.push(historyRecord);

                            } else {
                                failedSeatsList += seatInput + "(Booked) ";
                            }
                        } else {
                            failedSeatsList += seatInput + "(Invalid) ";
                        }
                    } catch (NumberFormatException e) {
                        failedSeatsList += seatInput + "(Error) ";
                    }
                } else {
                    failedSeatsList += seatInput + "(Invalid) ";
                }
            } else {
                failedSeatsList += seatInput + "(Error) ";
            }
        }

        System.out.println("\n=======================================");
        System.out.println("           Booking Summary");
        System.out.println("=======================================");
        if (successCount > 0) {
            double totalCost = successCount * prices[movieIndex];
            System.out.printf("Movie: %s\n", movies[movieIndex]);
            System.out.printf("Showtime: %s\n", showtimes[movieIndex]);
            System.out.printf("Successfully Booked: %s\n", bookedSeatsList);
            System.out.printf("Total Tickets: %d\n", successCount);
            System.out.printf("Total Price: %.2f THB\n", totalCost);
        } else {
            System.out.println("Status: No tickets were booked successfully.");
        }

        if (!failedSeatsList.isEmpty()) {
            System.out.println("---------------------------------------");
            System.out.println("[Warning] Failed to book: " + failedSeatsList);
        }
        System.out.println("=======================================\n");
    }

    // เมธอดสำหรับ Undo (ดึงข้อมูลล่าสุดออกมาคืนค่า)
    public static void undoBooking() {
        System.out.println("\n--- Undo Last Booking ---");

        // 1. เช็คก่อนว่ากล่อง Stack ว่างไหม (มีประวัติให้ลบหรือเปล่า?)
        if (bookingHistory.isEmpty()) {
            System.out.println("[Info] No booking history to undo. (ไม่มีประวัติการจองให้ยกเลิก)");
            return;
        }

        // =======================================================
        // 2. [หัวใจของ Stack: POP] ดึงข้อมูลใบบนสุด (ล่าสุด) ออกมา
        // เช่น ดึง "0,0,0,A1" ออกมา และข้อมูลนั้นจะถูกลบออกจาก Stack ด้วย
        // =======================================================
        String lastBooking = bookingHistory.pop();

        // 3. หั่นข้อความด้วยลูกน้ำ (,) คืนมาเป็น Array 4 ช่อง เพื่อเอา Index กลับมาใช้งาน
        String[] data = lastBooking.split(",");
        int mIndex = Integer.parseInt(data[0]); // เลขหนัง
        int rIndex = Integer.parseInt(data[1]); // เลขแถว
        int cIndex = Integer.parseInt(data[2]); // เลขคอลัมน์
        String seatName = data[3];              // ชื่อที่นั่ง (เช่น A1)

        // 4. เข้าไปใน 3D Array พิกัดเดิม แล้วคืนสถานะจาก 'X' กลับให้เป็น 'O' (ว่าง)
        seats[mIndex][rIndex][cIndex] = 'O';

        System.out.println("=======================================");
        System.out.println("        Undo Successful!");
        System.out.println("=======================================");
        System.out.printf("Cancelled Seat: %s\n", seatName);
        System.out.printf("Movie: %s\n", movies[mIndex]);
        System.out.println("Status: The seat is now available.");
        System.out.println("=======================================\n");
    }


    // =====================================================================================
    // [ส่วนที่ 4: Discussion & Conclusion] - Error Handling
    // การป้องกันไม่ให้โปรแกรมพัง (Crash) ด้วยการใช้ try-catch
    // =====================================================================================

    public static int getValidInput(Scanner scanner, String prompt, int min, int max) {
        int input = -1;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            try {
                input = scanner.nextInt();
                // ป้องกัน Index Out of Bounds
                if (input >= min && input <= max) {
                    isValid = true;
                } else {
                    System.out.println("[Error] Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                // ดักจับกรณีผู้ใช้พิมพ์ตัวอักษรแทนตัวเลข
                System.out.println("[Error] Invalid input. Please enter numbers only.");
                scanner.next();
            }
        }
        return input;
    }
}