package com.football.common.security;

/**
 * Hang so dung chung cho toan he thong khi lam viec voi JWT.
 *
 * CHU SO HUU MODULE: Hoang.
 * Cac thanh vien khac CHI DOC, khong sua truc tiep - can thay doi thi bao Hoang.
 *
 * Hoang se bo sung tiep vao package nay:
 *   - JwtService        : sinh token, doc claim, kiem tra chu ky va han dung
 *   - JwtAuthFilter     : doc header Authorization, nap SecurityContext
 *   - SecurityConfig    : khai bao endpoint cong khai / can dang nhap
 *   - RestAuthEntryPoint: tra ve 401 dung dinh dang JSON
 *   - RestAccessDeniedHandler: tra ve 403 dung dinh dang JSON
 */
public final class JwtConstants {

    /** Ten header mang token. */
    public static final String HEADER = "Authorization";

    /** Tien to bat buoc truoc chuoi token. */
    public static final String PREFIX = "Bearer ";

    /** Claim chua vai tro nguoi dung: CUSTOMER hoac OWNER. */
    public static final String CLAIM_ROLE = "role";

    /** Claim chua ho ten hien thi. */
    public static final String CLAIM_FULL_NAME = "fullName";

    /** Vai tro khach dat san. */
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    /** Vai tro chu san. */
    public static final String ROLE_OWNER = "OWNER";

    private JwtConstants() {
    }
}
