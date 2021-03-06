package ua.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.model.entity.Comment;
import ua.model.request.CommentRequest;
import ua.dto.MealDTO;
import ua.service.CommentService;
import ua.service.MealService;
import ua.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/menuItem/{id}")
public class MenuItemController {

    private final MealService service;

    private final CommentService commentService;

    private final UserService userService;

    private String error = "";

    @Autowired
    public MenuItemController(MealService service, CommentService commentService, UserService userService) {
        this.service = service;
        this.commentService = commentService;
        this.userService = userService;
    }

    @ModelAttribute("comment")
    public CommentRequest getForm() {
        return new CommentRequest();
    }

    /**
     * Show Menu Item page
     */
    @GetMapping
    public String show(Model model, @PathVariable String id) {
        MealDTO meal = service.findMealDTO(id);
        meal.setComments(service.findCommentList(id));
        model.addAttribute("meal", meal);
        model.addAttribute("tasteMeal", error);
        error = "";
        return "menuItem";
    }

    /**
     * Commenting and setting rate
     */
    @PostMapping
    public String mealIdCommentAndRate(@PathVariable String id,
                                       @ModelAttribute("comment") CommentRequest commentRequest) {
        List<String> userMealsIds = userService.findUserMealsIds();
        if (userMealsIds.contains(id)) {
            if (commentRequest.getRate() != null) {
                String commentId = commentService.saveComment(commentRequest);
                Comment comment = commentService.findById(commentId);
                if (!commentRequest.getText().isEmpty()) {
                    service.updateComments(id, comment);
                }
                service.updateMealRate(id, commentRequest.getRate());
            } else {
                error = "You must enter a rate!";
            }
        } else {
            error = "Taste the ingredient before the evaluation";
        }
        return "redirect:/menuItem/{id}";
    }

}