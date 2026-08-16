package com.smconsole.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.smconsole.excel.UserExcelService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserExcelService userExcelService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String loginId,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        Page<UserResponse> users = userService.getSearch(name, phone, loginId, status, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUser(@PathVariable Long id) {
        UserDetailResponse user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getStats() {
        return ResponseEntity.ok(userService.getStats());
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.update(id, request);
        return ResponseEntity.ok(user);
    }

    // 자동 판단 조건 없이 호출 즉시 DORMANT로 바뀌는 수동 처리다.
    @PutMapping("/{id}/dormant")
    public ResponseEntity<UserResponse> dormant(@PathVariable Long id) {
        UserResponse user = userService.dormant(id);
        return ResponseEntity.ok(user);
    }

    // 휴면(DORMANT) 상태였던 회원을 다시 정상(ACTIVE)으로 되돌린다.
    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activate(@PathVariable Long id) {
        UserResponse user = userService.activate(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> download() throws IOException {
        byte[] excelData = userExcelService.exportToExcel();

        // 파일명에 한글이 들어가서 그대로 헤더에 넣으면 깨진다. 인코딩 필수.
        String filename = "회원목록.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @PostMapping("/excel/upload")
    public ResponseEntity<String> upload (@RequestParam("file") MultipartFile file) throws IOException {
        userExcelService.importFromExcel(file);
        return ResponseEntity.ok("업로드가 완료되었습니다.");
    }


}

