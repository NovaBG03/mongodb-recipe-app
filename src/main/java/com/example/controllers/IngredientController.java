package com.example.controllers;

import com.example.commands.IngredientCommand;
import com.example.commands.RecipeCommand;
import com.example.commands.UnitOfMeasureCommand;
import com.example.domain.UnitOfMeasure;
import com.example.services.IngredientService;
import com.example.services.RecipeService;
import com.example.services.UnitOfMeasureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Slf4j
@Controller
public class IngredientController {

    private final IngredientService ingredientService;
    private final RecipeService recipeService;
    private final UnitOfMeasureService unitOfMeasureService;

    public IngredientController(IngredientService ingredientService, RecipeService recipeService, UnitOfMeasureService unitOfMeasureService) {
        this.ingredientService = ingredientService;
        this.recipeService = recipeService;
        this.unitOfMeasureService = unitOfMeasureService;
    }

    @GetMapping("/recipe/{recipeId}/ingredients")
    public String listIngredients(@PathVariable String recipeId, Model model){
        log.debug("Getting ingredient list for recipe id: " + recipeId);

        // use command object to avoid lazy load errors in Thymeleaf.
        model.addAttribute("recipe", recipeService.findCommandById(recipeId));

        return "recipe/ingredient/list";
    }

    @GetMapping("recipe/{recipeId}/ingredient/{id}/show")
    public String showRecipeIngredient(@PathVariable String recipeId,
                                       @PathVariable String id, Model model){
        model.addAttribute("ingredient", ingredientService.findByRecipeIdAndIngredientId(recipeId, id));
        return "recipe/ingredient/show";
    }

    @GetMapping("recipe/{recipeId}/ingredient/new")
    public String newIngredient(@PathVariable String recipeId, Model model){

        //make sure we have a good id value
        Mono<IngredientCommand> ingredientMono = recipeService.findCommandById(recipeId)
                .map(recipeCommand -> {
                    IngredientCommand ingredientCommand = new IngredientCommand();
                    ingredientCommand.setRecipeId(recipeCommand.getId());

                    ingredientCommand.setUom(new UnitOfMeasureCommand());

                    return ingredientCommand;
                });

        model.addAttribute("ingredient", ingredientMono);
        model.addAttribute("uomList",  unitOfMeasureService.listAllUoms());

        return "recipe/ingredient/ingredientform";
    }

    @GetMapping("recipe/{recipeId}/ingredient/{id}/update")
    public String updateRecipeIngredient(@PathVariable String recipeId,
                                         @PathVariable String id, Model model){
        Mono<IngredientCommand> ingredient = ingredientService.findByRecipeIdAndIngredientId(recipeId, id);
        model.addAttribute("ingredient", ingredient);

        Flux<UnitOfMeasureCommand> unitsOfMeasure = unitOfMeasureService.listAllUoms();
        model.addAttribute("uomList", unitsOfMeasure);
        return "recipe/ingredient/ingredientform";
    }

    @PostMapping("recipe/{recipeId}/ingredient")
    public String saveOrUpdate(@ModelAttribute IngredientCommand command){
        Mono<String> id = ingredientService.saveIngredientCommand(command)
                .map(ingredientCommand -> ingredientCommand.getId());

        log.debug("saved ingredient id:" + id);

        return "redirect:/recipe/" + command.getRecipeId() + "/ingredient/" + id + "/show";
    }

    @GetMapping("recipe/{recipeId}/ingredient/{id}/delete")
    public String deleteIngredient(@PathVariable String recipeId,
                                   @PathVariable String id){

        log.debug("deleting ingredient id:" + id);
        ingredientService.deleteById(recipeId, id);

        return "redirect:/recipe/" + recipeId + "/ingredients";
    }
}
