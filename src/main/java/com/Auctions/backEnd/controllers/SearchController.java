package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
import com.Auctions.backEnd.repositories.ItemRepository;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.services.Search.SortComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.Null;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/search")
public class SearchController extends BaseController{

    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    @Autowired
    public SearchController(ItemRepository itemRepository, ItemCategoryRepository itemCategoryRepository){
        this.itemRepository = itemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
    }


    @GetMapping("/partialMatch")
    public ResponseEntity getPartialMatchedSearch(@RequestParam String keyword){

        List<String> res = new ArrayList();

        if (keyword == null || keyword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid keyword"
            ));
        }

        keyword = keyword.toLowerCase();

        res.addAll(itemRepository.searchByName(keyword));
        res.addAll(itemRepository.searchByCategory(keyword));

        return ResponseEntity.ok(res);
    }


    /**
     * A User can use a search bar to find items/auctions based on:
     * the category name, the item's name and the item's description
     *
     * For every word of the text given we search for partial matching
     * in the above mentioned fields and we collect the results in a set
     * called 'res'. Afterwards, we sort the results on a best fit basis
     * i.e. the items appearing more times in the set are moved first in
     * the set
     *
     * The front-end part specifies a sublist range of the used as pagination
     * We return items of both open and completed auctions
     *
     * Algorithm for sorting elements by times of appearance was taken from:
     * https://www.geeksforgeeks.org/sort-elements-by-frequency-set-5-using-java-map/
     *
     * @param text - the keyword string
     * @param lower - the lower bound of the results sublist (inclusive)
     * @param upper - the lower bound of the results sublist (exclusive)
     * @return a list of items
     */
    @GetMapping("/searchBar")
    public ResponseEntity searchBar(@RequestBody String text,
                                    @RequestParam Integer lower,
                                    @RequestParam Integer upper){

        if(text == null || text.isEmpty() || lower == null || upper == null || lower < 0 || upper < lower){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "No keywords given or sublist range out of range"
            ));
        }

        List<Item> res = new ArrayList<>();

        //split string to words
        String[] values = text.split(" "); //TODO maybe extend to recognize ,-..
        for (String element : values) {
            res.addAll(itemRepository.searchItems(element));
        }

        Map<Item, Integer> map = new HashMap<>();
        List<Item> outputArray = new ArrayList<>();

        //assign elements and their count in the list and map
        for (Item current : res) {
            int count = map.getOrDefault(current, 0);
            map.put(current, count + 1);
            outputArray.add(current);
        }

        //compare the map by value
        SortComparator comp = new SortComparator(map);

        //sort the map using Collections CLass
        Collections.sort(outputArray, comp);

        LinkedHashSet<Item> hashSet = new LinkedHashSet<>(outputArray);

        ArrayList<Item> result = new ArrayList<>(hashSet);
        if(upper > result.size() - 1){
            return ResponseEntity.ok(result.subList(lower, result.size()));
        }

        return ResponseEntity.ok(result.subList(lower, upper));
    }


    /**
     * https://www.baeldung.com/java-lists-intersection
     */
//    @GetMapping("/filters")
//    public ResponseEntity filterSearch(@Nullable @RequestParam Long categoryId,
//                                       @Nullable @RequestParam Double lowerPrice,
//                                       @Nullable @RequestParam Double higherPrice,
//                                       @Nullable @RequestParam String locationTitle,
//                                       @Nullable @RequestParam String description){
//
//        List<Item> results = new ArrayList<>();
//
//        //search according to category parameters
//        if(categoryId != null) {
//
//            ItemCategory category = itemCategoryRepository.findItemCategoryById(categoryId);
//            if(category == null){
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                        "Error",
//                        "Category not found"
//                ));
//            }
//            results.retainAll(category.getItems());
//        }
//
//        //search according to price parameters
//        if(lowerPrice != null && higherPrice != null){
//
//            List<Item> byPrice = itemRepository.searchByPrice(lowerPrice, higherPrice);
//            if(byPrice == null){
//                return ResponseEntity.ok(null);
//            }
//            if(results.isEmpty()){
//                results.addAll(byPrice);
//            }
//            else{
//                results.retainAll(byPrice);
//            }
//        }
//        else if(higherPrice != null){
//
//            List<Item> byHigherPrice = itemRepository.searchByHigherPrice(higherPrice);
//            if(byHigherPrice == null){
//                return ResponseEntity.ok(null);
//            }
//            if(results.isEmpty()){
//                results.addAll(byHigherPrice);
//            }
//            else{
//                results.retainAll(byHigherPrice);
//            }
//        }
//        else if(lowerPrice != null){
//
//            List<Item> byLowerPrice = itemRepository.searchByLowerPrice(lowerPrice);
//            if(byLowerPrice == null){
//                return ResponseEntity.ok(null);
//            }
//            if(results.isEmpty()){
//                results.addAll(byLowerPrice);
//            }
//            else{
//                results.retainAll(byLowerPrice);
//            }
//        }
//
//        //search according to location parameter
//        if(locationTitle != null){
//
//            List<Item> byLocationTitle = itemRepository.searchByLocation(locationTitle);
//            if(byLocationTitle == null){
//                return ResponseEntity.ok(null);
//            }
//            if(results.isEmpty()){
//                results.addAll(byLocationTitle);
//            }
//            else{
//                results.retainAll(byLocationTitle);
//            }
//        }
//
//
//        //search according to description parameter
//        if(description != null){
//
//            List<Item> byDescription = itemRepository.searchByDescription(description);
//            if(byDescription == null){
//                return ResponseEntity.ok(null);
//            }
//            if(results.isEmpty()){
//                results.addAll(byDescription);
//            }
//            else{
//                results.retainAll(byDescription);
//            }
//        }
//
//        return ResponseEntity.ok(results);
//    }



    public Page findByCriteria(String categoryName, Double lowerPrice, Double higherPrice,
                               String locationTitle, String description, Pageable pageable){

        return itemRepository.findAll(new Specification<Item>() {
            @Override
            public Predicate toPredicate(Root<Item> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if(categoryName != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("categories").get("name"), categoryName)));
                }

                if(lowerPrice != null && higherPrice != null){
                    predicates.add(criteriaBuilder.between(root.get("currently"), lowerPrice, higherPrice));
                }
                else if(higherPrice != null){
                    predicates.add(criteriaBuilder.le(root.get("currently"), higherPrice));
                }
                else if(lowerPrice != null){
                    predicates.add(criteriaBuilder.ge(root.get("currently"), lowerPrice));
                }

                if(description != null){
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%")));
                }
                if(locationTitle != null){
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(criteriaBuilder.lower(
                            root.get("location").get("locationTitle")),"%" + locationTitle.toLowerCase() + "%")));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
    }

    /**
     * https://javadeveloperzone.com/spring/spring-jpa-dynamic-query-example/
     *
     * @param categoryName - exact name of the category (case-sensitive)
     * @param lowerPrice - lower price of items
     * @param higherPrice - higher price of items
     * @param locationTitle - location of auction (non case-sensitive)
     * @param description - description of an item (non case-sensitive)
     * @param pageable - pageable (page number, page size, optional sorting)
     * @return
     */
    @GetMapping("/filters")
    public ResponseEntity filterSearch(@Nullable @RequestParam String categoryName,
                                       @Nullable @RequestParam Double lowerPrice,
                                       @Nullable @RequestParam Double higherPrice,
                                       @Nullable @RequestParam String locationTitle,
                                       @Nullable @RequestParam String description,
                                       Pageable pageable){

        return ResponseEntity.ok(findByCriteria(categoryName, lowerPrice, higherPrice, locationTitle, description, pageable));
    }

}
