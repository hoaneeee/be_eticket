package com.example.E_Ticket.service;

public interface QrService {
    /*tạo file PNG QR từ content, trả về đường dẫn tương đối (vd: uploads/qr/xxx.png) */
    String createPng(String content, String fileNameNoExt, int size);
}
