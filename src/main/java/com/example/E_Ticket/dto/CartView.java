    /*
    package com.example.E_Ticket.dto;

    import java.util.ArrayList;
    import java.util.List;

    */
    /** Dùng cho view giỏ hàng *//*

    public class CartView {
        public static class Line {
            public Long ticketTypeId;
            public Long eventId;
            public String eventTitle;
            public String ticketTypeName;
            public long unitPrice; // VND
            public int qty;

            public long lineTotal(){ return unitPrice * qty; }
            public Line() {}
        }

        public List<Line> lines = new ArrayList<>();
        public long grandTotal(){
            return lines.stream().mapToLong(Line::lineTotal).sum();
        }

    }
    */
    // src/main/java/com/example/E_Ticket/dto/CartView.java
    package com.example.E_Ticket.dto;

    import java.io.Serializable;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.List;

    public class CartView implements Serializable {
        private static final long serialVersionUID = 1L;

        public List<Line> lines = new ArrayList<>();

        public long grandTotal(){
            return lines.stream().mapToLong(Line::lineTotal).sum();
        }

        public static class Line implements Serializable {
            private static final long serialVersionUID = 1L;

            public Long eventId;
            public Long ticketTypeId;
            public String eventTitle;
            public String ticketTypeName;
            public long unitPrice;  // VND
            public int qty;
            public Long holdId;
            public Instant holdExpiresAt;
            public String zoneName;
            public String seatNo;

            public long lineTotal(){ return unitPrice * qty; }
        }
    }
