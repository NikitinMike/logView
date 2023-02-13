package ru.olabank.demoview;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Comparator.comparing;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;
import static ru.olabank.demoview.ReadAuthLogsService.*;

@Controller
@Slf4j
@SpringBootApplication
public class MainController {

    static final SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
    static long oneDay = 3600000L * 24;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    @DateTimeFormat(pattern = "dd/MM/yyyy")
    Date date = new Date();
    String order = "";
    boolean errorsOnly = false;

    @GetMapping("/{date}")
    @ResponseBody
    public ModelAndView dataPageGet(Model model, @PathVariable String date) throws ParseException {
//        log.info("Date in {} / {} ", date, this.date);
        if (date == null || "favicon.ico".equals(date)) this.date = new Date();
        if (date != null && date.matches("....-..-..")) this.date = dtf.parse(date);
        model.addAttribute("date", getDate());
        model.addAttribute("messages", messages());
//        log.info("Date out {} ", this.date);
        return new ModelAndView("homePage");
    }

    HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        HttpServletRequest request = ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String s = e.nextElement();
            if (s != null) headers.put(s, request.getHeader(s));
        }
        log.info("GET {} Ok", request.getRemoteAddr());
        return headers;
    }

    //    @ModelAttribute("messages")
    public List<DataMessage> messages() {
        if (date.after(new Date())) date = new Date();
        List<DataMessage> list = readAuthLog(date);
        if (errorsOnly) list.removeIf(l -> !l.phone.equals(""));
        switch (order) {
            case "phone":
                list.sort(Comparator.nullsLast(comparing(l -> l.phone)));
                break;
            case "ip":
                list.sort(comparing(l -> l.ip));
                break;
            case "login":
                list.sort(comparing(l -> l.login));
                break;
            default:
                break;
        }
        Collections.reverse(list);
        return list;
    }

    @ModelAttribute("date")
    public String getDate() {
        return dtf.format(date);
    }

    @GetMapping({"/", "/start"})
    @ResponseBody
    public ModelAndView startPageGet() {
        date = new Date();
        order = "";
        return new ModelAndView("redirect:/" + moveDate(0));
    }

    @GetMapping("/errors")
    @ResponseBody
    public ModelAndView errorsOnly() {
        errorsOnly = !errorsOnly;
        return new ModelAndView("redirect:/" + moveDate(0));
    }

    @GetMapping("/list")
    @ResponseBody
    public ModelAndView listPageGet(Model model) throws IOException {
        List<String> files = readFiles();
        Collections.reverse(files);
        model.addAttribute("date", getDate());
        model.addAttribute("files", files); //  singleton("")
        return new ModelAndView("list");
    }

    @GetMapping("/order/{order}")
    @ResponseBody
    public ModelAndView setOrder(@PathVariable String order) {
        this.order = "";
        if (order.matches("ip|login|phone")) this.order = order.trim();
        return new ModelAndView("redirect:/" + dtf.format(date));
    }

    @GetMapping("/yesterday")
    public ModelAndView leftPageGet() {
        return new ModelAndView("redirect:/" + moveDate(-1));
    }

    @GetMapping("/week")
    public ModelAndView weekPageGet() {
        return new ModelAndView("redirect:/" + moveDate(-7));
    }

    @GetMapping("/month")
    public ModelAndView monthPageGet() {
        return new ModelAndView("redirect:/" + moveDate(-30));
    }

    @GetMapping("/nextday")
    public ModelAndView rightPageGet() {
        return new ModelAndView("redirect:/" + moveDate(1));
    }

    @GetMapping("/nextweek")
    public ModelAndView rightWeekPageGet() {
        return new ModelAndView("redirect:/" + moveDate(7));
    }

    String moveDate(int days) {
        if (days != 0)
            while (checkFile(new Date(date.getTime() + oneDay * days)) == null)
                if (days > 999 || days < -999) break;
                else days += (days < 0) ? -1 : 1;
        if (new Date(date.getTime() + oneDay * days).after(new Date())) date = new Date();
        else date = new Date(date.getTime() + oneDay * days);
        return dtf.format(date);
    }
}