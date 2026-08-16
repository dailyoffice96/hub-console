package com.smconsole.excel;

import com.smconsole.user.User;
import com.smconsole.user.UserRepository;
import com.smconsole.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CacheEvict;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserExcelService {

    private final UserRepository userRepository;

    public byte[] exportToExcel() throws IOException {
        List<User> users = userRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("회원목록");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("번호");
        headerRow.createCell(1).setCellValue("이름");
        headerRow.createCell(2).setCellValue("아이디");
        headerRow.createCell(3).setCellValue("전화번호");
        headerRow.createCell(4).setCellValue("이메일");
        headerRow.createCell(5).setCellValue("상태");
        headerRow.createCell(6).setCellValue("가입일");

        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getLoginId());
            row.createCell(3).setCellValue(user.getPhone() != null ? user.getPhone() : "");
            row.createCell(4).setCellValue(user.getEmail() != null ? user.getEmail() : "");
            row.createCell(5).setCellValue(user.getStatus().name());
            row.createCell(6).setCellValue(user.getCreatedAt().toString());

            rowNum++;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    @CacheEvict(value = "userStats", allEntries = true)
    public String importFromExcel(MultipartFile file) throws IOException {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum();

        String name = null;
        String loginId = null;
        for (int i = 1; i <= rowNum; i++) {
            Row row = sheet.getRow(i);

            name = row.getCell(0).getStringCellValue();
            loginId = row.getCell(1).getStringCellValue();
            String phone = row.getCell(2).getStringCellValue();
            String email = row.getCell(3).getStringCellValue();

            if (name == null || name.isBlank() || loginId == null || loginId.isBlank()) {
                throw new IllegalArgumentException((i + 1) + "번째 행: 이름 또는 아이디를 입력해 주세요.");
            }

            if (userRepository.findByLoginId(loginId).isPresent()) {
                throw new IllegalArgumentException((i + 1) + "번째 행: 이미 존재하는 아이디입니다.");
            }

            User user = new User();
            user.setName(name);
            user.setLoginId(loginId);
            user.setPhone(phone);
            user.setEmail(email);
            user.setStatus(UserStatus.ACTIVE);

            userRepository.save(user);
        }


        return "회원 등록이 완료되었습니다.";
    }
}
