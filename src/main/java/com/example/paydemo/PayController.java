package com.example.paydemo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PayController {
    @GetMapping("/")//tu custom URL
    public String pay(@RequestParam String vnp_OrderInfo, //Thong tin thanh toan cho don hang tim hieu them tren vnpay developer
                      @RequestParam String vnp_TxnRef, //Tuong tu nhu OrderID, ton tai duy nhat
                      @RequestParam String vnp_IpAddr, //Ip Address Of Customer
                      @RequestParam String amountStr
//                      @RequestParam String txt_billing_mobile,
//                      @RequestParam String txt_billing_email,
//                      @RequestParam String txt_billing_fullname,
//                      @RequestParam String txt_inv_addr1,
//                      @RequestParam String txt_bill_city,
//                      @RequestParam String txt_bill_country,
//                      @RequestParam String txt_inv_mobile,
//                      @RequestParam String txt_inv_email,
//                      @RequestParam String txt_inv_customer,
//                      @RequestParam String txt_inv_company,
//                      @RequestParam String txt_inv_taxcode,
//                      @RequestParam String cbo_inv_type

    ) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";

        String orderType = "100000"; //Ma loại hàng hóa vì bán thực phẩm nên để fix cứng luôn

        String vnp_TmnCode = "3ENL7XVD";

        int amount = Integer.parseInt(amountStr) * 100;
        Map vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn"); //Giao dien hien thi ngon ngu de mac dinh la TV

        vnp_Params.put("vnp_ReturnUrl", "/");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        String vnp_CreateDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//        cld.add(Calendar.MINUTE, 15);
//        String vnp_ExpireDate = formatter.format(cld.getTime());
//        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
//        //Billing
//
//        vnp_Params.put("vnp_Bill_Mobile", txt_billing_mobile);
//        vnp_Params.put("vnp_Bill_Email", txt_billing_email);
//        String fullName = (txt_billing_fullname).trim();
//        if (fullName != null && !fullName.isEmpty()) {
//            int idx = fullName.indexOf(' ');
//            String firstName = fullName.substring(0, idx);
//            String lastName = fullName.substring(fullName.lastIndexOf(' ') + 1);
//            vnp_Params.put("vnp_Bill_FirstName", firstName);
//            vnp_Params.put("vnp_Bill_LastName", lastName);
//
//        }
//        vnp_Params.put("vnp_Bill_Address", txt_inv_addr1);
//        vnp_Params.put("vnp_Bill_City", txt_bill_city);
//        vnp_Params.put("vnp_Bill_Country", txt_bill_country);
//
//        // Invoice
//        vnp_Params.put("vnp_Inv_Phone", txt_inv_mobile);
//        vnp_Params.put("vnp_Inv_Email", txt_inv_email);
//        vnp_Params.put("vnp_Inv_Customer", txt_inv_customer);
//        vnp_Params.put("vnp_Inv_Address", txt_inv_addr1);
//        vnp_Params.put("vnp_Inv_Company", txt_inv_company);
//        vnp_Params.put("vnp_Inv_Taxcode", txt_inv_taxcode);
//        vnp_Params.put("vnp_Inv_Type", cbo_inv_type);
        //Build data to hash and querystring
        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                if(!fieldName.equals("vnp_ReturnUrl")||fieldName.equals("vnp_IpAddr")||!fieldName.equals("vnp_Locale")
                ||!fieldName.equals("vnp_OrderType")||!fieldName.equals("vnp_Version")||!fieldName.equals("vnp_Command")
                ||!fieldName.equals("vnp_CurrCode")) {
//                    hashData.append(fieldName);
//                    hashData.append('=');
//                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    hashData.append(fieldValue);
                }
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String random = "abc";
        byte[] salt = random.getBytes();
        MessageDigest md = MessageDigest.getInstance("SHA512");
        md.update(salt);
        String vnp_SecureHash = Arrays.toString(md.digest(hashData.toString().getBytes(StandardCharsets.UTF_8)));
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" + "?" + queryUrl;
        return "redirect:" + queryUrl;
    }


    @GetMapping("https://sandbox.vnpayment.vn/tryitnow/Home/VnPayIPN")
    public void getIPN(@PathVariable String vnp_Amount,
                       @PathVariable String vnp_OrderInfo,
                       @PathVariable String vnp_ResponseCode,
                       @PathVariable String vnp_TmnCode,
                       @PathVariable String vnp_TxnRef,
                       @PathVariable String vnp_SecureHashType,
                       @PathVariable String vnp_SecureHash) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Map vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                if(!fieldName.equals("vnp_ReturnUrl")||fieldName.equals("vnp_IpAddr")||!fieldName.equals("vnp_Locale")
                        ||!fieldName.equals("vnp_OrderType")||!fieldName.equals("vnp_Version")||!fieldName.equals("vnp_Command")
                        ||!fieldName.equals("vnp_CurrCode")) {
//                    hashData.append(fieldName);
//                    hashData.append('=');
//                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    hashData.append(fieldValue);
                }
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String random = "abc";
        byte[] salt = random.getBytes();
        MessageDigest md = MessageDigest.getInstance(vnp_SecureHashType);
        md.update(salt);
        String signValue = Arrays.toString(md.digest(hashData.toString().getBytes(StandardCharsets.UTF_8)));
        if(signValue.equals(vnp_SecureHash)){

            boolean checkOrderId = true; //viết function kt xem vnp_TxnRef có tồn tại trong database không
            boolean checkAmount = true; //viết function kiểm tra vnp_Amount is valid (Check vnp_Amount VNPAY returns compared to the amount of the code (vnp_TxnRef) in the Your database).
            boolean checkOrderStatus = true; // PaymnentStatus = 0 (pending)
            if(checkOrderId){
                if(checkAmount){
                    if("00".equals(vnp_ResponseCode)){
                        //update order status to 1
                    }else{
                        //Giao dịch không thành công làm gì tiếp thì làm
                    }
                }else{
                    //bắn ra 1 cái exception hay 1 cái j đấy
                }
            }else{
                //bắn ra 1 cái exception hay 1 cái j đấy
            }
        }
    }


}
