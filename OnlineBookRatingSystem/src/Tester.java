
/* Name : H. P. Hewamalage              Index No : 110220J */

import java.util.Date;
import java.text.*;
import java.io.*;
import java.util.Scanner;

public class Tester {
    String date, time, user, product, vendor, vendor_rating, book_rating;
    OnlineStore market;
    
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
        Tester test = new Tester();
        test.createMarket();
        test.readInput();
        test.search();
    }
    
    public void createMarket(){
        market = new OnlineStore();                         //create an online market 
    }
    
    public void readInput() throws FileNotFoundException, IOException, ParseException{
        
        BufferedReader br = new BufferedReader( new FileReader( "input.txt" ));
        String rating = br.readLine();             //reading strings from the input file
        String array2[];
        String array1[];
        while( rating != null  ){
            array2 = rating.split("\\s+");
            array1 = array2[0].split("T");           //splitting the string appropriately
            
            date = array1[0];               //initializing the variables
            time = array1[1];
            user = array2[1];
            product = array2[2]; 
            vendor = array2[3]; 
            vendor_rating = array2[4];
            book_rating = array2[5];
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date dt = formatter.parse( date + " " + time );
            Rating rt = new Rating( dt, user, vendor, product,  Integer.parseInt( book_rating ), Integer.parseInt(vendor_rating ));           //create a Rating object with the data just read from the file
            market.receiveRating( rt );         //send the Rating to the online market
            
            rating = br.readLine();     //read the next line from the input file
            
        
        }
    }
    
    public void search(){
        
        Scanner input = new Scanner( System.in );
        System.out.println( "\nEnter the name of the book:" );            
        String book = input.nextLine();        //get the name of the book to search for
        market.searchBook(book);
        System.out.println( "\nEnter the name of the vendor:" );
        String vndr = input.nextLine();         //get the name of the vendor to search for
        market.searchVendor(vndr);
        
    }
}

abstract class Super{            //super class of all classes that need to be inserted into the hash tables and linked lists. 
    String name;
    public Super( String nm ){
        name = nm;
    }
}
class Product extends Super{
 
    LinkedList raters;            //stores the list of users who rate the product 
    Vendor topVendors[];          //heap to store the list of the top rated vendors
    int vendor_heap_size;         //heap size of the vendor heap
    Rating recentRatings[];       //heap to store the most recent ratings
    int rating_heap_size;        //heap size of the rating heap
    
    public Product( String nm ){
        super( nm );
        raters = new LinkedList();
        topVendors = new Vendor[5];
        vendor_heap_size = 0;
        recentRatings = new Rating[5];
        rating_heap_size = 0;
 
    }
    
    public void updateRecentRatings( Rating rt ){
        if( rating_heap_size < 5 ){                //if the rating heap is not full
            recentRatings[rating_heap_size++] = rt;   //accept any new rating
            build_min_heap_rating();                    //and build a min heap from the existing ratings
        }
        else if( rt.date_time.after( recentRatings[1].date_time ) ){       //else if the rating heap is full and the current rating is earlier than the earliest rating in the heap
            recentRatings[0] = rt;                                         //insert the current rating into the heap
            min_heapify_rating(0);                                         //heapify the min heap and keep the heap property 
        }
    }
    
    public void build_min_heap_rating(){                                         //code to build min heap
        for( int i = (rating_heap_size-1)/2; i >=0 ;i-- ){
            min_heapify_rating(i);
        }
    }
    
    public void min_heapify_rating( int i ){            //code to min heapify
        int l = 2*i + 1;
        int r = 2*i + 2;
        int min;
        Rating temp;
        if( l< rating_heap_size && recentRatings[l].date_time.before(recentRatings[i].date_time)) min = l;
        else min = i;
        if( r< rating_heap_size && recentRatings[r].date_time.before(recentRatings[min].date_time)) min = r;
        if( min != i ){
            temp = recentRatings[i];
            recentRatings[i] = recentRatings[min];
            recentRatings[min]=temp;
            min_heapify_rating( min );
        }
       
    }
    
    public void updateTopVendors( Vendor vndr ){         //method to update the heap of top vendors for the product 
        boolean contains = false;                   
        for( int i = 0; i < vendor_heap_size; i++ ){      //check if the vendor heap already contains the current vendor  
            if( topVendors[i] == vndr ){                  
                contains = true;
                break;
            }
        }
        if( !contains && vendor_heap_size<5 ){             //if the vendor heap does not contain the current vendor and the heap is not full
            topVendors[vendor_heap_size++] = vndr;        //add the vendor into the heap
            build_min_heap_vendor();                     //build a min heap from the existing vendors
        }
        else if( !contains && topVendors[0].getAggregateRating() < vndr.getAggregateRating() ){      //else if the heap does not contain the current vendor and the 
            topVendors[0] = vndr;                      //the current vendor's aggregate rating is higher than the lowest rating in the heap, add the vendor into the heap  
            min_heapify_vendor(0);                //min heapify to mainatain the heap property                                        
        }
    }
    
    public void build_min_heap_vendor(){               //code to build min heap
        for( int i = (vendor_heap_size-1)/2; i >=0 ;i-- ){
            min_heapify_vendor(i);
        }
    }
    
    public void min_heapify_vendor( int i ){       //code to do min heapify
        int l = 2*i + 1;
        int r = 2*i + 2;
        int min;
        Vendor temp;
        if( l< vendor_heap_size && topVendors[l].getAggregateRating()< topVendors[i].getAggregateRating()) min = l;
        else min = i;
        if( r< vendor_heap_size && topVendors[r].getAggregateRating()< topVendors[min].getAggregateRating()) min = r;
        if( min != i ){
            temp = topVendors[i];
            topVendors[i] = topVendors[min];
            topVendors[min]=temp;
            min_heapify_vendor( min );
        }
    }

    
    public double getAggregateRating( ){            //returning the aggregate rate of the product
        double upper = 0;
        double lower = 0;
        Node x = raters.head;
        User usr; 
        for( int i =1; i <= raters.count; i++ ){      //iterate through the raters list of the product and calculate the aggregate rating 
            usr = ( User )x.element;
            upper += usr.getWeight()*((BookRating)usr.bookRatings.chainedHashSearch(name)).bookRatingSum;
            lower += usr.getWeight()*((BookRating)usr.bookRatings.chainedHashSearch(name)).bookRatingCount;
            x = x.next;
        }
        return upper/lower;
    }
    
    public void printRecentRatings(){                     //prints the most recent ratings of the product
        for( int i=0; i<rating_heap_size;i++ ){
            System.out.println( recentRatings[i].getBookRating());
        }
    }
    
    public void printTopVendors(){                    //prints the top rated vendors of the product
        for( int i=0; i<vendor_heap_size;i++ ){
            System.out.println( topVendors[i].name );
        }
    }
}

class Vendor extends Super{
    LinkedList books;
    LinkedList raters;
    Rating recentRatings[];
    int rating_heap_size;
    
    public Vendor( String nm ){
        super( nm );
        books = new LinkedList();
        raters = new LinkedList();
        recentRatings = new Rating[5];
        rating_heap_size = 0;
    }
    public void updateRecentRating( Rating rt ){            //code to update the heap of the most recent ratings
        if( rating_heap_size < 5 ){
            recentRatings[rating_heap_size++] = rt;
            build_min_heap_rating();
        }
        else if( rt.date_time.after( recentRatings[1].date_time ) ){
            recentRatings[0] = rt;
            min_heapify_rating(0);
        }
    }
    
    public void build_min_heap_rating(){
        for( int i = (rating_heap_size-1)/2; i >=0 ;i-- ){
            min_heapify_rating(i);
        }
    }
    
    public void min_heapify_rating( int i){
        int l = 2*i + 1;
        int r = 2*i + 2;
        int min;
        Rating temp;
        if( l< rating_heap_size && recentRatings[l].date_time.before(recentRatings[i].date_time)) min = l;
        else min = i;
        if( r< rating_heap_size && recentRatings[r].date_time.before(recentRatings[min].date_time)) min = r;
        if( min != i ){
            temp = recentRatings[i];
            recentRatings[i] = recentRatings[min];
            recentRatings[min]=temp;
            min_heapify_rating( min );
        }
    }
    public double getAggregateRating(){      //iterate through  the raters list of the vendor and calculate the aggregate rating 
        double upper = 0.0;
        double lower = 0.0;
        Node x = raters.head;
        User usr;
        for( int i =1; i <= raters.count; i++ ){
            usr = ( User )x.element;
            upper += usr.getWeight()*((VendorRating)usr.vendorRatings.chainedHashSearch(name)).vendorRatingSum;
            lower += usr.getWeight()*((VendorRating)usr.vendorRatings.chainedHashSearch(name)).vendorRatingCount;
            x = x.next;
        }
        return upper/lower;
    }
    
    public void printRecentRatings(){
        for( int i=0; i<rating_heap_size;i++ ){
            System.out.println( recentRatings[i].getVendorRating());
        }
    }
    
    public void printProductList(){                 //iterate through the product list of the vendor and print both the product name and the its aggregate rating
        Node x = books.head;
        Product bk;
        for( int i=1; i<= books.count; i++ ){
            bk = ( Product )x.element;
            System.out.printf("%s %f\n", bk.name,bk.getAggregateRating());
            x = x.next;
        }
    }
}

class User extends Super{
    double ratingCount = 0;
    LinkedList books;
    LinkedList sellers;
    
    HashTable vendorRatings;
    HashTable bookRatings;
    
    
    public User( String nm ){ //sode to initialze the name, and the linked listst and the hash tables of the user
        super(nm);
        books = new LinkedList();
        sellers = new LinkedList();
        vendorRatings = new HashTable();
        bookRatings = new HashTable();
    }
    
    public double getWeight(){
        return ( 2.0 - 1.0/ratingCount );       //returns the weight of the user
    }
    
    
}

class BookRating extends Super{
   
    int bookRatingCount;  //keeps the number of times the user has rated for the book defined by the String variable name
    int bookRatingSum;   //keeps the rating sum for the number of times the user has rated for the book defined by the String variable name
    
    public BookRating( String nm, int rate ){
        super(nm);
        bookRatingCount = 1;
        bookRatingSum = rate;
    }
    
    public String toString(){
        return String.format( "%d %d \n", bookRatingCount,bookRatingSum );
    }
}

class VendorRating extends Super{
   
    int vendorRatingCount;   //keeps the number of times the user has rated for the vendor defined by the String variable name
    int vendorRatingSum;     //keeps the rating sum for the number of times the user has rated for the vendor defined by the String variable name
    
    public VendorRating( String nm, int rate ){
        super(nm);
        vendorRatingCount = 1;
        vendorRatingSum = rate;
    }
    public String toString(){
        return String.format( "%d %d \n", vendorRatingCount,vendorRatingSum );
    }
}

class Rating{
    Date date_time;
    String user;
    String seller;
    String book;
    int bookRating;
    int vendorRating;
    
    public Rating( Date dt, String rater, String vndr, String bk, int bkRate, int vndrRate ){
        date_time = dt;       //code to initialize the current rating
        user = rater;
        seller = vndr;
        book = bk;
        bookRating = bkRate;
        vendorRating = vndrRate;
    }
    
    public String getBookRating(){       //returns details of a book rating
        return String.format ( "%s %s %d\n",date_time, user, bookRating );
    }
    
    public String getVendorRating(){     //returns details of a vendor rating
        return String.format( "%s %s %d\n",date_time, user, vendorRating );
    }
    
}

class OnlineStore{
    HashTable onlineRaters = new HashTable();
    HashTable sellers = new HashTable();
    HashTable books = new HashTable(); 
    
    public void receiveRating( Rating rt ){
        Product bk = ( Product )books.chainedHashSearch( rt.book );      //search if the book is already available in the market
        Vendor vndr = ( Vendor )sellers.chainedHashSearch(rt.seller);     //search if the vendor is already available in the market
        User rater = ( User )onlineRaters.chainedHashSearch(rt.user);     //search if the user has done any rating before
        if( bk == null ){           //If the book has not been rated before
            bk = new Product(rt.book);          //create a new book object with the specified name
            books.chainedHashInsert(bk);          //insert the new book into the online market
        }
   
        bk.updateRecentRatings( rt );           //update the list of most recent ratings for the book
        if( vndr == null ){                     // If the vendor has not been rated before
            vndr = new Vendor(rt.seller);        //create a new vendor object with the specified name
            sellers.chainedHashInsert(vndr);      //insert the vendor into the online market
        }
        vndr.updateRecentRating(rt);                 //update the list of most recent ratings for the vendor
        if( rater == null ){                       //If the rater has not rated in the online system before 
            rater = new User( rt.user );          //create a new user object with the specified name  
            onlineRaters.chainedHashInsert(rater);     //insert the new rater into the online market
        }
        rater.ratingCount++;                     //update the rating count of the rater
  //      System.out.println( "-----------------" + rater.ratingCount);
        
        if( rater.books.listSearch(rt.book) == null ){    //if the rater has not rated for this particular book before
            rater.books.listInsert( new Node( bk));        //insert the book into the books list of the rater
            bk.raters.listInsert( new Node( rater));       //insert the rater into the raters list of the book 
            rater.bookRatings.chainedHashInsert( new BookRating( rt.book,rt.bookRating ) );   //set the rating sum and the rating count of this
                                                                                                 //particular book for this particular user
        }
          
        else{                                                               //else if the rater has rated this particular book before                
            BookRating rt1=( BookRating ) rater.bookRatings.chainedHashSearch(rt.book);      
            rt1.bookRatingCount++;            //update the rating count of this particular book for this particular user
            rt1.bookRatingSum += rt.bookRating;//update the rating sum of this particular book for this particular user                   
        } 
        if( rater.sellers.listSearch(rt.seller) == null ){         //if the rater has not rated this particular vendor before
            rater.sellers.listInsert( new Node( vndr ));      //insert the vendor into the vendors list of the rater
            vndr.raters.listInsert( new Node( rater));        //insert the rater into the raters list of the vendor
            rater.vendorRatings.chainedHashInsert( new VendorRating( rt.seller,rt.vendorRating ) );     //set the rating count and the rating sum of this
                                                                                                     //particular vendor for this particular user
        }
          
        else{     //else if the rater has rated this vendor before                                                                          
            VendorRating rt2=( VendorRating ) rater.vendorRatings.chainedHashSearch(rt.seller);    
            rt2.vendorRatingCount++;     //update the rating count of this particular vendor for this particular user
            rt2.vendorRatingSum += rt.vendorRating;  //update the rating sum of this particular vendor for this particualr user                  
        } 
        
        bk.updateTopVendors(vndr);     //update the list of top rated vendors of the book
        if( vndr.books.listSearch(rt.book) == null ) vndr.books.listInsert(new Node(bk));           //insert this book into the product list of this vendor
     }
    
    public void searchBook( String book ){      
        Product prdct = (Product)books.chainedHashSearch(book);      //search for a given book
        if( prdct == null ) System.out.println("No such book found");     //print erro message if the book is not found
        else{
            System.out.printf("\nOverall aggregate rating: %f\n", prdct.getAggregateRating());    //print the aggregate rating
            System.out.println("\nMost recent ratings:");
            prdct.printRecentRatings();        //print the most recent ratings of the book
            System.out.println("\nTop rated vendors:");    
            prdct.printTopVendors();     //print the top rated vendors of the book
        }
    }
    
    public void searchVendor( String vendor ){
        Vendor seller = (Vendor)sellers.chainedHashSearch(vendor);
        if( seller == null ) System.out.println( "No such vendor found");
        else{
            System.out.printf("\nOverall aggregate rating: %f\n", seller.getAggregateRating());
            System.out.println( "\nMost recent ratings:" );
            seller.printRecentRatings();
            System.out.println( "\nList of products and their overall aggregate ratings: ");
            seller.printProductList();
        }
    }
    
}

class LinkedList{
    Node head;
    Node tail;
    int count = 0;   //variable to keep track of the number of elements inserted into the linked list
    
    public void listInsert( Node x ){
        count++;
        if ( head == null ){
            head = x;
        }
        else{
            tail.next = x;
        }
        x.previous = tail;
        tail = x;
        x.next = null;
        
    }
    
    public Super listSearch( String k ){
        Node x = head;
        
    
        while( x!= null && !k.equals(x.element.name)){
            x = x.next;
        }
        if( x == null ) return null;  //return null if the object with the particular name is not found
        else return (x.element);  //return a reference to the object if it is found
 
    }
}

class Node{
    
    Node previous;
    Super element;
    Node next;
    
    public Node( Super elmnt ){
        
        element = elmnt;
    }
    
}

class HashTable{
    LinkedList array[] = new LinkedList[47];
    int key = 0;
    
    public HashTable(){         //initializing the hash table with an array of linked lists
        for(int i =0; i<47;i ++ ){
            array[i] = new LinkedList();
        }
    }
    public void chainedHashInsert( Super x ){
        key = 0;
        for( int i=0; i < x.name.length(); i++ ){
            key += x.name.charAt( i );             //calculate the key value for a given object to be stored
        }
        array[key % 47].listInsert(new Node(x));    //insert the object into the correct position of the corresponding linked list
        
    }
    
    public Super chainedHashSearch( String name ){
        key = 0;
        for( int i=0; i < name.length(); i++ ){
            key += name.charAt( i );
        }
        return array[key % 47].listSearch(name);
    }
}


