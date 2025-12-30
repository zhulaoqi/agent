package com.kinch.agent.service;

import com.kinch.agent.dto.PptRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * PPT生成服务
 * 使用AI分析内容并生成PPT
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PptGenerationService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * 生成PPT
     */
    public byte[] generatePpt(PptRequest request) throws IOException {
        log.info("生成PPT - 主题: {}", request.getTheme());

        // 1. 使用AI分析内容，生成结构化大纲
        PptOutline outline = generateOutline(request.getTheme(), request.getContent());

        // 2. 创建PPT
        XMLSlideShow ppt = new XMLSlideShow();

        // 3. 创建标题页
        createTitleSlide(ppt, outline.getTitle(), outline.getSubtitle());

        // 4. 创建内容页
        for (PptOutline.Slide slideContent : outline.getSlides()) {
            createContentSlide(ppt, slideContent.getTitle(), slideContent.getPoints());
        }

        // 5. 创建结束页
        createEndSlide(ppt, "谢谢观看");

        // 6. 转换为字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ppt.write(out);
        ppt.close();

        log.info("PPT生成完成，页数: {}", ppt.getSlides().size());
        return out.toByteArray();
    }

    /**
     * 使用AI生成PPT大纲
     */
    private PptOutline generateOutline(String theme, String content) {
        log.info("AI生成PPT大纲");

        BeanOutputConverter<PptOutline> converter = new BeanOutputConverter<>(PptOutline.class);

        String prompt = String.format("""
            请根据以下主题和内容，生成一个PPT大纲：
            
            主题：%s
            内容：%s
            
            要求：
            1. 生成标题和副标题
            2. 将内容分解为3-6个主要部分
            3. 每个部分包含标题和3-5个要点
            4. 要点要简洁，每条不超过20字
            
            %s
            """, theme, content, converter.getFormat());

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return converter.convert(response);
    }

    /**
     * 创建标题页
     */
    private void createTitleSlide(XMLSlideShow ppt, String title, String subtitle) {
        XSLFSlide slide = ppt.createSlide();
        
        // 标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(50, 100, 600, 100));
        XSLFTextParagraph titleParagraph = titleBox.addNewTextParagraph();
        titleParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun titleRun = titleParagraph.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(44.0);
        titleRun.setFontColor(new Color(31, 73, 125));
        titleRun.setBold(true);

        // 副标题
        XSLFTextBox subtitleBox = slide.createTextBox();
        subtitleBox.setAnchor(new Rectangle(50, 220, 600, 50));
        XSLFTextParagraph subtitleParagraph = subtitleBox.addNewTextParagraph();
        subtitleParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun subtitleRun = subtitleParagraph.addNewTextRun();
        subtitleRun.setText(subtitle);
        subtitleRun.setFontSize(24.0);
        subtitleRun.setFontColor(new Color(89, 89, 89));
    }

    /**
     * 创建内容页
     */
    private void createContentSlide(XMLSlideShow ppt, String title, List<String> points) {
        XSLFSlide slide = ppt.createSlide();

        // 标题
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(50, 40, 600, 60));
        XSLFTextParagraph titleParagraph = titleBox.addNewTextParagraph();
        XSLFTextRun titleRun = titleParagraph.addNewTextRun();
        titleRun.setText(title);
        titleRun.setFontSize(32.0);
        titleRun.setFontColor(new Color(31, 73, 125));
        titleRun.setBold(true);

        // 内容要点
        XSLFTextBox contentBox = slide.createTextBox();
        contentBox.setAnchor(new Rectangle(80, 120, 580, 350));
        
        for (String point : points) {
            XSLFTextParagraph paragraph = contentBox.addNewTextParagraph();
            paragraph.setBullet(true);
            paragraph.setIndentLevel(0);
            paragraph.setBulletFontColor(new Color(31, 73, 125));
            paragraph.setSpaceBefore(10.0);
            
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(point);
            run.setFontSize(20.0);
            run.setFontColor(Color.BLACK);
        }
    }

    /**
     * 创建结束页
     */
    private void createEndSlide(XMLSlideShow ppt, String message) {
        XSLFSlide slide = ppt.createSlide();

        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new Rectangle(200, 200, 300, 100));
        XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
        paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(message);
        run.setFontSize(40.0);
        run.setFontColor(new Color(31, 73, 125));
        run.setBold(true);
    }

    /**
     * PPT大纲
     */
    public static class PptOutline {
        public String title;
        public String subtitle;
        public List<Slide> slides;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        public List<Slide> getSlides() { return slides; }
        public void setSlides(List<Slide> slides) { this.slides = slides; }

        public static class Slide {
            public String title;
            public List<String> points;

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            public List<String> getPoints() { return points; }
            public void setPoints(List<String> points) { this.points = points; }
        }
    }
}


