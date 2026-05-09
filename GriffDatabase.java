import java.sql.*;
import java.util.Scanner;

//Program creates Database of tools containing tables griff_brand and griff_tool.
class GriffDatabase 
{
    //Fields:
    private static String URL="jdbc:oracle:thin:@//174.50.132.143:1521/FREEPDB1";
    private static String USER_NAME="csus";
    private static String PASS_WORD="spring";   

    //Predefined Strings for setup and drop
    private static String griff_brand = """
        CREATE TABLE griff_brand (
            brandID NUMBER PRIMARY KEY,
            brandName VARCHAR2(30) UNIQUE NOT NULL)
    """;
                                         
    private static String griff_tool = """
        CREATE TABLE griff_tool (
            toolID NUMBER PRIMARY KEY,
            toolName VARCHAR2(30),
            brandID NUMBER NOT NULL,
            CONSTRAINT griff_part_brandID_fk
            FOREIGN KEY (brandID) REFERENCES griff_brand(brandID))
    """; 

    private static String griff_brand_brandID_seq = """
            CREATE SEQUENCE griff_brand_brandID_seq START WITH 1 INCREMENT BY 1
    """;

    private static String griff_tool_toolID_seq = """
            CREATE SEQUENCE griff_tool_toolID_seq START WITH 1 INCREMENT BY 1
    """;

    private static String drop_griff_brand = """
        DROP TABLE griff_brand
    """;


    private static String drop_griff_tool = """
        DROP TABLE griff_tool
    """;

    private static String drop_griff_tool_toolID_seq = """
            DROP SEQUENCE griff_tool_toolID_seq
    """;

    private static String drop_griff_brand_brandID_seq = """
            DROP SEQUENCE griff_brand_brandID_seq
    """;

    public static void main(String[] args)throws Exception
    {
        DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

        try(Connection con=DriverManager.getConnection(URL, USER_NAME, PASS_WORD);)
        {
            setupDatebase(con);
            runMenu(con);
            dropTables(con);
            dropSequence(con);
        }
        catch(SQLException e)
        {
            System.out.println( " the error is " + e);
            e.printStackTrace();
        }

    }//End main()

    //Methods:
    public static void setupDatebase(Connection con){
        
        //Drop to ensure clean tables 
        // then Create 
        dropTables(con);
        dropSequence(con);
        createTables(con);
        createSequence(con);

    }//End setupDatabase()

    //Diplay menu for user selections
    //  --- Tool Database Menu ---
    //     1) Insert
    //     2) Delete
    //     3) Update
    //     4) View
    //     5) Quit
    //     Enter choice:
    public static void runMenu(Connection con){
    
        Scanner input = new Scanner(System.in);
        int i = -1;

        //Loop until the user enters 5
        while(i != 5){
            System.out.print(
                "-- Tool Database Menu --\n" +
                "\t1) Insert\n" +
                "\t2) Delete\n" +
                "\t3) Update\n" +
                "\t4) View\n" +
                "\t5) Quit\n" + 
                "Enter Option: "
            );

            i = input.nextInt(); 
            System.out.println();
            input.nextLine();

            switch(i) {
                case 1:
                    insert(con, input);
                    break;
                case 2:
                    delete(con, input);
                    break;
                case 3:
                    update(con, input);
                    break;
                case 4:
                    view(con, input);
                    break;
                case 5:
                    //Quit Program.
                    break;
                default:
                    //Prompt for entry.
            }

        }//End while

        input.close();

    }//End runMenu()

    //Method to Drop all tables
    public static void dropTables(Connection con){

        try( Statement dropTable = con.createStatement()){
            dropTable.executeUpdate(drop_griff_tool);
            dropTable.executeUpdate(drop_griff_brand);
            System.out.println("Tables dropped: griff_tool, griff_brand\n");
        }
        catch(SQLException e){
            System.out.println("Tables did not exist.\n");
            System.out.println(e.getMessage());
        }

    }//End dropTables()

    //Method to create tables
    public static void createTables(Connection con){                     

        try( Statement tableGriffBrand = con.createStatement()) {
            tableGriffBrand.executeUpdate(griff_brand);
            tableGriffBrand.executeUpdate(griff_tool);
            System.out.println("Created Tables: griff_brand, griff_tool\n");
        }
        catch(SQLException e){
            System.out.println("Error creating tables.\n");
            System.out.println(e.getMessage());
        }

    }//End createTables()
    
    //Method to drop sequence
    public static void dropSequence(Connection con){

        try(Statement dropSequence = con.createStatement()){
            dropSequence.executeUpdate(drop_griff_brand_brandID_seq);
            dropSequence.executeUpdate(drop_griff_tool_toolID_seq);
            System.out.println("Sequences dropped.\n");
        }
        catch(SQLException e){
            System.out.println("Error dropping the sequences.");
            System.out.println(e.getMessage());
        }

    }//End dropSequence()

    //Method to create sequence 
    public static void createSequence(Connection con){

        try(Statement createSequence = con.createStatement()){
            createSequence.executeUpdate(griff_brand_brandID_seq);
            createSequence.executeUpdate(griff_tool_toolID_seq);
            System.out.println("Sequences created.\n");
        }
        catch(SQLException e){
            System.out.println("Error creating the sequences.");
            System.out.println(e.getMessage());
        }

    }//End createSequence()
    
    //Method for inserting into the tables
    public static void insert(Connection con, Scanner input){

        String insertBrand = """
                INSERT INTO griff_brand VALUES (griff_brand_brandID_seq.NEXTVAL, ?)
        """;

        String getBrandID = """
                SELECT brandID FROM griff_brand WHERE brandName = ?
        """;
       
        String insertTool = """
                INSERT INTO griff_tool VALUES (griff_tool_toolID_seq.NEXTVAL, ?, ?)
        """;
        
        System.out.println("Enter the tools brand:");
        String brand = input.nextLine();    
        
        //Insert the tools brand into griff_brand
        try (PreparedStatement ps = con.prepareStatement(insertBrand))
        {
            ps.setString(1, brand);
            ps.executeUpdate();
            System.out.println("Brand inserted.");

        } catch(SQLException e)
        {
            System.out.println("Brand Insert failure.");
            System.out.println(e.getMessage());
        }
        
        System.out.println("Enter the name of the tool:");
        String name = input.nextLine();
 
        //Insert the tool name into griff_tool
        try (
            PreparedStatement ps2 = con.prepareStatement(getBrandID);
            PreparedStatement ps3 = con.prepareStatement(insertTool)
        ){
            ps2.setString(1, brand);
            ResultSet rs = ps2.executeQuery();
            int brandID = -1;
            if (rs.next())
            {
                brandID = rs.getInt("brandID");
            }
            ps3.setString(1, name);
            ps3.setInt(2, brandID);
            ps3.executeUpdate();
            System.out.println("Tool inserted.");

        } catch(SQLException e)
        {
            System.out.println("Tool Insert failure.");
            System.out.println(e.getMessage());
        }

    }//End insert()

    public static void delete(Connection con, Scanner input){}

    public static void update(Connection con, Scanner input){}

    public static void view(Connection con, Scanner input){
        // View brandName toolName 
        String viewContents = """
                SELECT toolName, brandName FROM griff_tool
                JOIN griff_brand USING (brandID)
        """;

        try( Statement viewRecords = con.createStatement()) {
            ResultSet records = viewRecords.executeQuery(viewContents);
            while (records.next())
            {
                int i = 1;
                String child = records.getString(i);
                String parent = records.getString(i+1);
                System.out.println(child + "\t" + parent);
                i++;
            }
            System.out.println(); 
        }
        catch(SQLException e){
            System.out.println("Error creating tables.\n");
            System.out.println(e.getMessage());
        }
    }

}//End class GriffDatabase()