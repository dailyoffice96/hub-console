package com.smconsole.excel;

import com.smconsole.auditlog.AuditLog;
import com.smconsole.auditlog.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogExcelService {

    // 메모리엔 이 행 수만큼만 들고 있고, 그 이상은 임시 파일로 flush한다 (SXSSF 스트리밍 방식).
    private static final int ROW_ACCESS_WINDOW_SIZE = 100;

    private final AuditLogRepository auditLogRepository;

    public byte[] exportToExcel() {
        // findAll() 대신 admin을 fetch join하는 쿼리를 써서, 아래 반복문에서 auditLog.getAdmin()을
        // 부를 때마다 지연 로딩 쿼리가 하나씩 더 나가는 N+1을 막는다.
        List<AuditLog> auditLogs = auditLogRepository.findAllFetchAsList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        SXSSFWorkbook workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        try {
            Sheet sheet = workbook.createSheet("감사로그 목록");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("번호");
            headerRow.createCell(1).setCellValue("담당자");
            headerRow.createCell(2).setCellValue("변경타입");
            headerRow.createCell(3).setCellValue("변경상태");
            headerRow.createCell(4).setCellValue("내용");
            headerRow.createCell(5).setCellValue("일시");

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

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            log.error("감사로그 엑셀 생성 실패 (대상 {}건)", auditLogs.size(), e);
            throw new ExcelExportException("엑셀 파일 생성 중 오류가 발생했습니다.", e);
        } finally {
            // SXSSFWorkbook은 rowAccessWindowSize를 넘는 행을 디스크 임시 파일로 내려쓴다.
            // dispose()를 안 부르면 그 임시 파일이 안 지워지고 계속 쌓인다.
            workbook.dispose();
            try {
                workbook.close();
            } catch (IOException e) {
                log.warn("엑셀 워크북 종료 중 오류(무시 가능)", e);
            }
        }
    }
}
