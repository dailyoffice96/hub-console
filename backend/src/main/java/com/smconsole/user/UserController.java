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

    // 즉시 휴면 전환: 자동 판단 조건 없이, 호출 즉시 해당 회원을 DORMANT로 바꾸는 수동 처리입니다.
    @PutMapping("/{id}/dormant")
    public ResponseEntity<UserResponse> dormant(@PathVariable Long id) {
        UserResponse user = userService.dormant(id);
        return ResponseEntity.ok(user);
    }

    // 휴면 해제: 휴면(DORMANT) 상태였던 회원을 다시 정상(ACTIVE)으로 되돌립니다.
    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activate(@PathVariable Long id) {
        UserResponse user = userService.activate(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/excel")
    //"HTTP 요청을 받아서, 응답을 만들어 돌려주는" 역할 download()
    public ResponseEntity<byte[]> download() throws IOException {
        // 실제 엑셀 데이터를 만들어야 함
        // "실제로 엑셀 파일 데이터를 만드는" 역할
        byte[] excelData = userExcelService.exportToExcel();

        // 파일명 만들기(한글 포함이라서 인코딩이 필요함)
        String filename = "회원목록.xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        //응답 헤더 직접 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);

        //헤더 + 데이터를 합쳐서 ResponseEntity로 반환
        //그냥 바이트 덩어리만 받으면 → "이걸 화면에 텍스트로 보여줘야 하나? 이미지인가? 뭔지 모르겠다"

        // ResponseEntity.ok(data) = 편의점 도시락 (그냥 담겨있는 대로 파는 것, 포장지 커스텀 안 됨)
        // new ResponseEntity<>(data, headers, status) = 직접 포장하는 것 (안에 뭘 넣을지, 겉에 어떤 라벨을 붙일지 다 내가 정함)
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

    }

    //파일을 받아서 Service에 그대로 넘기고, 성공하면 메시지만 돌려
    @PostMapping("/excel/upload")
    public ResponseEntity<String> upload (@RequestParam("file") MultipartFile file) throws IOException {
        userExcelService.importFromExcel(file);
        return ResponseEntity.ok("업로드가 완료되었습니다.");
    }


}

