import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // =========================================================================
    // [ส่วนที่ 1] รับผิดชอบโดย: คนที่ 1 (จัดการโครงสร้างข้อมูล ArrayList และเมนูหลัก)
    // =========================================================================

    // เปลี่ยนมาใช้ ArrayList สำหรับเก็บข้อมูลที่สามารถยืดหดได้
    static ArrayList<String> movies = new ArrayList<>();
    static ArrayList<String> showtimes = new ArrayList<>();
    static ArrayList<Double> prices = new ArrayList<>();

    // ผังที่นั่งใช้ ArrayList เก็บ Array 2 มิติ (เพื่อให้จัดการง่าย ไม่ซับซ้อนเกินไป)
    static ArrayList<boolean[][]> bookedSeats = new ArrayList<>();

    // Method สำหรับเตรียมข้อมูลเริ่มต้น (อธิบายอาจารย์ว่าเราใช้ .add() เพิ่มข้อมูล)
    public static void setupData() {
        movies.add("Avenger: Endgame");
        showtimes.add("13:00");
        prices.add(250.00);
        bookedSeats.add(new boolean[5][10]); // สร้างผังที่นั่ง 5 แถว 10 คอลัมน์ ให้หนังเรื่องที่ 1

        movies.add("Inception");
        showtimes.add("15:30");
        prices.add(200.00);
        bookedSeats.add(new boolean[5][10]); // สร้างผังที่นั่งให้หนังเรื่องที่ 2

        movies.add("Interstellar");
        showtimes.add("19:00");
        prices.add(180.00);
        bookedSeats.add(new boolean[5][10]); // สร้างผังที่นั่งให้หนังเรื่องที่ 3
    }

    public static void main(String[] args) {
        setupData(); // โหลดข้อมูลหนังเข้าระบบก่อนเริ่มโปรแกรม

        Scanner input = new Scanner(System.in);
        boolean isRunning = true;

        while (isRunning) {
            System.out.println("\n=======================================");
            System.out.println("   Welcome to Cinema Booking System");
            System.out.println("=======================================");
            System.out.println("--- Main Menu ---");
            System.out.println("1. View Movies (ดูรายชื่อหนังและรอบฉาย)");
            System.out.println("2. View Seats (ดูผังที่นั่ง)");
            System.out.println("3. Book Ticket(s) (จองตั๋ว)");
            System.out.println("4. Exit (ออกจากระบบ)");
            System.out.print("Please select an option (1-4): ");

            int choice = input.nextInt();

            switch (choice) {
                case 1:
                    viewMovies();
                    break;
                case 2:
                    viewSeatsFlow(input);
                    break;
                case 3:
                    bookTicketFlow(input);
                    break;
                case 4:
                    isRunning = false;
                    System.out.println("\nThank you for using the system. Goodbye!");
                    break;
                default:
                    System.out.println("\n[Error] Invalid option. Please try again.");
            }
        }
    }

    public static void viewMovies() {
        System.out.println("\n--- Now Showing ---");
        // ArrayList ใช้ .size() แทน .length
        for (int i = 0; i < movies.size(); i++) {
            // ArrayList ใช้ .get(i) ดึงข้อมูล แทนการใช้ [i]
            System.out.printf("%d. %s | Showtime: %s | %.2f THB\n",
                    (i + 1), movies.get(i), showtimes.get(i), prices.get(i));
        }
    }

    // =========================================================================
    // [ส่วนที่ 2] รับผิดชอบโดย: คนที่ 2 (จัดการระบบแสดงผล UI และผังที่นั่ง)
    // =========================================================================

    public static void viewSeatsFlow(Scanner input) {
        viewMovies();
        System.out.print("Select a movie to view seats (1-" + movies.size() + "): ");
        int movieChoice = input.nextInt();

        int movieIndex = movieChoice - 1;
        if (movieIndex >= 0 && movieIndex < movies.size()) {
            displaySeats(movieIndex);
        } else {
            System.out.println("[Error] Invalid movie selection.");
        }
    }

    public static void displaySeats(int movieIndex) {
        System.out.println("\n--- Screen for: " + movies.get(movieIndex) + " (" + showtimes.get(movieIndex) + ") ---");

        System.out.print("     ");
        for (int i = 1; i <= 10; i++) {
            System.out.print(i + "   ");
        }
        System.out.println();

        char[] rows = {'A', 'B', 'C', 'D', 'E'};

        // ดึงผังที่นั่งของหนังเรื่องนั้นๆ ออกมาจาก ArrayList ก่อน
        boolean[][] currentSeats = bookedSeats.get(movieIndex);

        for (int r = 0; r < 5; r++) {
            System.out.print(rows[r] + "  ");
            for (int c = 0; c < 10; c++) {
                if (currentSeats[r][c]) {
                    System.out.print("[X] "); // จองแล้ว
                } else {
                    System.out.print("[ ] "); // ว่าง
                }
            }
            System.out.println();
        }
        System.out.println("Legend: '[ ]' = Available | '[X]' = Booked");
    }

    // =========================================================================
    // [ส่วนที่ 3] รับผิดชอบโดย: คนที่ 3 (จัดการตรรกะการจอง, ดักจับบัค และพิมพ์ใบเสร็จ)
    // =========================================================================

    public static void bookTicketFlow(Scanner input) {
        System.out.println("\n--- Book Ticket(s) ---");
        viewMovies();
        System.out.print("Select a movie (1-" + movies.size() + "): ");
        int movieChoice = input.nextInt();

        int movieIndex = movieChoice - 1;
        if (movieIndex < 0 || movieIndex >= movies.size()) {
            System.out.println("[Error] Invalid movie selection.");
            return;
        }

        displaySeats(movieIndex);

        input.nextLine();
        System.out.print("\nSelect your seat(s) separated by space (e.g., A1 A2 B1) or type '0' to cancel: ");
        String seatInput = input.nextLine();

        if (seatInput.trim().equals("0")) {
            System.out.println("Booking cancelled.");
            return;
        }

        processBooking(movieIndex, seatInput);
    }

    public static void processBooking(int movieIndex, String seatInput) {
        String cleanInput = seatInput.replace(",", " ").trim();
        String[] seatsToBook = cleanInput.split("\\s+");

        String successList = "";
        String failList = "";
        int successCount = 0;
        double totalPrice = 0;

        // ดึงผังที่นั่งของหนังเรื่องที่เลือกมาจัดการ
        boolean[][] currentSeats = bookedSeats.get(movieIndex);

        for (int i = 0; i < seatsToBook.length; i++) {
            String seat = seatsToBook[i].toUpperCase();
            if (seat.isEmpty()) continue;

            if (seat.length() < 2) {
                failList += seat + " (Invalid Format), ";
                continue;
            }

            char rowChar = seat.charAt(0);
            int r = rowChar - 'A';
            int c = -1;

            try {
                c = Integer.parseInt(seat.substring(1)) - 1;
            } catch (Exception e) {
                failList += seat + " (Invalid Number), ";
                continue;
            }

            if (r >= 0 && r < 5 && c >= 0 && c < 10) {
                if (!currentSeats[r][c]) {
                    currentSeats[r][c] = true; // อัปเดตสถานะการจอง
                    successList += seat + " ";
                    successCount++;
                    totalPrice += prices.get(movieIndex); // ดึงราคาจาก ArrayList
                } else {
                    failList += seat + " (Already Booked), ";
                }
            } else {
                failList += seat + " (Not Found), ";
            }
        }

        // พิมพ์ใบเสร็จ
        System.out.println("\n=======================================");
        System.out.println("            Booking Summary");
        System.out.println("=======================================");
        System.out.println("Movie: " + movies.get(movieIndex));
        System.out.println("Showtime: " + showtimes.get(movieIndex));

        if (successCount > 0) {
            System.out.println("Successfully Booked: " + successList);
            System.out.println("Total Tickets: " + successCount);
            System.out.printf("Total Price: %.2f THB\n", totalPrice);
        } else {
            System.out.println("Successfully Booked: None");
        }

        if (!failList.isEmpty()) {
            System.out.println("---------------------------------------");
            System.out.println("[Warning] Failed to book: " + failList);
            System.out.println("Please check seat availability and try again.");
        }
        System.out.println("=======================================");
    }
}