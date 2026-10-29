package com.fishing.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {
    // Ảnh mới được phục vụ trực tiếp từ Cloudinary.
    // Giữ servlet để tránh ảnh hưởng cấu trúc hiện tại.
}