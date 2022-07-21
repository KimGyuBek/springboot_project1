package jpabook;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
//    model애 데이터를 실어서 뷰에 넘길수 있다.
    public String hello(Model model) {
        model.addAttribute("data", "hello!!");

//        화면이름, .html이 자동으로 붙는다
        return "hello";
    }


}
