package com.smconsole.excel;

/** 엑셀 파일 생성 중 발생한 실패를 감싸서, 원인 예외의 원시 메시지를 그대로 클라이언트에 흘려보내지 않고
 *  일관된 메시지로 응답하기 위한 전용 예외. */
public class ExcelExportException extends RuntimeException {

    public ExcelExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
