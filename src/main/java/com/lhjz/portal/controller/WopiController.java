package com.lhjz.portal.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lhjz.portal.entity.File;
import com.lhjz.portal.entity.Setting;
import com.lhjz.portal.model.RespBody;
import com.lhjz.portal.repository.FileRepository;
import com.lhjz.portal.repository.SettingRepository;
import com.lhjz.portal.pojo.Enum.SettingType;
import com.lhjz.portal.util.StringUtil;
import com.lhjz.portal.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("api/wopi")
public class WopiController {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Value("${file.download.url}")
    private String fileDownloadUrl;

    private Map<String, Object> getWopiSettings() {
        Setting setting = settingRepository.findOneBySettingType(SettingType.Wopi);
        if (setting == null || setting.getContent() == null) {
            throw new RuntimeException("WOPI settings not found");
        }
        return JsonUtil.json2Object(setting.getContent(), Map.class);
    }

    @GetMapping("/{uuid}")
    public RespBody getFileInfo(@PathVariable String uuid, HttpServletRequest request) {
        File file = fileRepository.findByUuid(uuid);
        if (file == null) {
            return RespBody.failed("File not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("file", file);
        response.put("config", generateEditorConfig(file, request));

        return RespBody.succeed(response);
    }

    @PostMapping("/{uuid}")
    public RespBody updateFile(@PathVariable String uuid, @RequestBody String content) {
        File file = fileRepository.findByUuid(uuid);
        if (file == null) {
            return RespBody.failed("File not found");
        }
        // Here you would typically update the file content
        // For this example, we'll just update the name
        file.setName(file.getName() + " (edited)");
        fileRepository.save(file);
        return RespBody.succeed(file);
    }

    private Map<String, Object> generateEditorConfig(File file, HttpServletRequest request) {
        Map<String, Object> wopiSettings = getWopiSettings();
        String editorUrl = (String) wopiSettings.get("editorUrl");
        String jwtSecret = (String) wopiSettings.get("jwtSecret");

        Map<String, Object> config = new HashMap<>();

        Map<String, Object> document = new HashMap<>();
        document.put("fileType", StringUtil.getFileExtension(file.getName()));
        document.put("key", file.getUuidName());
        document.put("title", file.getName());

        // 构建绝对 URL
        String absoluteFileUrl = UriComponentsBuilder.fromHttpUrl(getBaseUrl(request))
                .path(fileDownloadUrl)
                .path(file.getUuid())
                .toUriString();
        document.put("url", absoluteFileUrl);

        config.put("document", document);
        config.put("documentType", getDocumentType(file.getName()));

        Map<String, Object> editorConfig = new HashMap<>();
        // 同样使用绝对 URL 构建 callbackUrl
        String absoluteCallbackUrl = UriComponentsBuilder.fromHttpUrl(getBaseUrl(request))
                .path("/api/wopi/")
                .path(file.getUuidName())
                .toUriString();
        editorConfig.put("callbackUrl", absoluteCallbackUrl);

        Map<String, String> user = new HashMap<>();
        user.put("id", "demo");
        user.put("name", "John Doe");
        editorConfig.put("user", user);

        editorConfig.put("lang", "zh");
        config.put("editorConfig", editorConfig);

        config.put("editorUrl", editorUrl);
        String token = createToken(config, jwtSecret);
        config.put("token", token);

        return config;
    }

    private String createToken(Map<String, Object> payload, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withPayload(payload)
                    .sign(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDocumentType(String fileName) {
        String extension = StringUtil.getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case "doc":
            case "docx":
                return "word";
            case "xls":
            case "xlsx":
                return "cell";
            case "ppt":
            case "pptx":
                return "slide";
            default:
                return "word";
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        // 构建基础 URL
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);

        return url.toString();
    }

    @GetMapping("/content/{uuid}")
    public RedirectView getFileContent(@PathVariable String uuid) {
        return new RedirectView(fileDownloadUrl + uuid);
    }
}