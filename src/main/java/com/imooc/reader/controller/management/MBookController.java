package com.imooc.reader.controller.management;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.imooc.reader.entity.Book;
import com.imooc.reader.service.BookService;
import com.imooc.reader.service.exception.BussinessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/management/book")
public class MBookController {
    @Resource
    BookService bookService;

    @GetMapping("/index.html")
    public ModelAndView showBook () {
        return new ModelAndView("/management/book");
    }

    @GetMapping("/comment.html")
    public ModelAndView showBookComment () {
        return new ModelAndView("/management/comment");
    }

    @PostMapping("/upload")
    @ResponseBody
    public Map upload(@RequestParam("img") MultipartFile file, HttpServletRequest request) throws IOException {
        // 得到上传目录
        String uploadPath = request.getServletContext().getResource("/").getPath() + "/upload/";
        // 文件名
        String fileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        //扩展名
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        //保存文件到upload目录
        file.transferTo(new File(uploadPath + fileName + suffix));
        Map result = new HashMap();
        result.put("errno", 0);
        result.put("data", new String[]{"/upload/" + fileName + suffix});
        return result;
    }

    @PostMapping("/create")
    @ResponseBody
    public Map createBook (Book book) {
        Map result = new HashMap();
        try {
            book.setEvaluationQuantity(0);
            book.setEvaluationScore(0f);
            Document document = Jsoup.parse(book.getDescription()); // 解析图书详情
            Element img = document.select("img").first();
            String cover = img.attr("src");
            book.setCover(cover); // 来自于descripttion描述的第一幅图
            bookService.createBook(book);
            result.put("code", "0");
            result.put("msg", "success");
        } catch (BussinessException e) {
            e.printStackTrace();
            result.put("code", e.getCode());
            result.put("msg", e.getMsg());
        }
        return result;
    }

    @GetMapping("/list")
    @ResponseBody
    public Map list (Integer page, Integer limit) {
        if (page == null) {
            page = 1;
        }
        if (limit == null) {
            limit = 10;
        }
        IPage<Book> pageObject = bookService.paging(null, null, page, limit);
        Map result = new HashMap();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", pageObject.getRecords()); // 当前页数据
        result.put("count", pageObject.getTotal()); // 未分页时记录总数
        return result;
    }

    @GetMapping("/id/{id}")
    @ResponseBody
    public Map selectById (@PathVariable("id") Long bookId) {
        Book book = bookService.selectById(bookId);
        Map result = new HashMap();
        result.put("code", "0");
        result.put("msg", "success");
        result.put("data", book);
        return result;
    }

    /**
     * 更新图书数据
     * @param book 图书数据
     * @return 处理结果
     */
    @PostMapping("/update")
    @ResponseBody
    public Map updateBook (Book book) {
        Map result = new HashMap();
        try {
            Book rawBook = bookService.selectById(book.getBookId());
            rawBook.setBookName(book.getBookName());
            rawBook.setSubTitle(book.getSubTitle());
            rawBook.setAuthor(book.getAuthor());
            rawBook.setCategoryId(book.getCategoryId());
            rawBook.setDescription(book.getDescription());
            Document document = Jsoup.parse(book.getDescription());
            String cover = document.select("img").first().attr("src");
            rawBook.setCover(cover);
            bookService.updateBook(rawBook);
            result.put("code", "0");
            result.put("msg", "success");
        } catch (BussinessException e) {
            e.printStackTrace();
            result.put("code", e.getCode());
            result.put("msg", e.getMsg());
        }
        return result;
    }

    @GetMapping("/delete/{id}")
    @ResponseBody
    public Map deleteBook (@PathVariable("id") Long bookId) {
        Map result = new HashMap();
        try {
            bookService.deleteBook(bookId);
            result.put("code", "0");
            result.put("msg", "success");
        } catch (BussinessException e) {
            e.printStackTrace();
            result.put("code", e.getCode());
            result.put("msg", e.getMsg());
        }
        return result;
    }
}
