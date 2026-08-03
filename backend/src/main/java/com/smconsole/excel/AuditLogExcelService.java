package com.smconsole.excel;

import com.smconsole.auditlog.AuditLog;
import com.smconsole.auditlog.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogExcelService {

    private final AuditLogRepository auditLogRepository;

    public byte[] exportToExcel() throws IOException {
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Workbook, Sheet 만들기
        // 파일(Workbook) → 그 안에 시트(Sheet)들 → 시트 안에 행(Row)들 → 행 안에 셀(Cell)들
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("감사로그 목록");

        //헤더 행 만들기
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("번호");
        headerRow.createCell(1).setCellValue("담당자");
        headerRow.createCell(2).setCellValue("변경타입");
        headerRow.createCell(3).setCellValue("변경상태");
        headerRow.createCell(4).setCellValue("내용");
        headerRow.createCell(5).setCellValue("일시");

        //반복문으로 데이터 채우기
        int rowNum = 1;
        for (AuditLog auditLog : auditLogs) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(auditLog.getAdmin() != null ? auditLog.getAdmin().getName() : "");
            row.createCell(2).setCellValue(auditLog.getTargetType().name());
            row.createCell(3).setCellValue(auditLog.getAction().name());
            row.createCell(4).setCellValue(auditLog.getDetail());
            row.createCell(5).setCellValue(auditLog.getCreatedAt().format(formatter));

            rowNum++;
        }

        //ByteArrayOutputStream으로 변환
        //자바 메모리 안에 있는 객체일 뿐, 아직 "파일"이 아니에요. 이걸 실제로 브라우저에 전송하려면 바이트(byte) 형태로 바꿔야 함
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
}
