package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
import com.Auctions.backEnd.repositories.ItemRepository;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.responses.ResultPage;
import com.Auctions.backEnd.services.Search.SortComparator;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import java.util.*;

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


    /**
     * A user can get a list of suggestions when typing in the search bar
     *
     * @param keyword - partial or full word to be matched
     * @return the list of suggestiong
     */
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
     * @return a list of items
     */
    @PutMapping("/searchBar")
    public ResponseEntity searchBar(@RequestBody String text,
                                    Pageable pageable){

        if(text == null || text.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "No keywords given"
            ));
        }

        List<Item> res = new ArrayList<>();

        //split string to words
        String[] values = text.split(" ");
        for (String element : values) {
            res.addAll(itemRepository.searchItems(element.toLowerCase()));
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

        ArrayList<Item> result = new ArrayList<>(new LinkedHashSet<>(outputArray));

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int totalElements = result.size();

        if(totalElements == 0){
            return ResponseEntity.ok(new ResultPage(null, totalElements, 0));
        }

        List<List<Item>> subset = Lists.partition(result, size);

        return ResponseEntity.ok(new ResultPage(subset.get(page),
                totalElements, (int)Math.ceil((double)totalElements / size)));
    }



    public Page findByCriteria(String categoryId, Double lowerPrice, Double higherPrice,
                               String locationTitle, String description, Pageable pageable){

        return itemRepository.findAll(new Specification<Item>() {
            @Override
            public Predicate toPredicate(Root<Item> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if(categoryId != null) {
                    Join<Item, ItemCategory> join = root.join("categories", JoinType.LEFT);
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(join.get("id"), categoryId)));
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
     * A user can make a query using specific criteria
     * We use h helper function to return a page result (findByCriteria)
     *
     * https://javadeveloperzone.com/spring/spring-jpa-dynamic-query-example/
     *
     * @param categoryId - exact name of the category (case-sensitive)
     * @param lowerPrice - lower price of items
     * @param higherPrice - higher price of items
     * @param locationTitle - location of auction (non case-sensitive)
     * @param description - description of an item (non case-sensitive)
     * @param pageable - pageable (page number, page size, optional sorting)
     *
     * @return - a list of matches (all parameters matched)
     */
    @GetMapping("/filters")
    public ResponseEntity filterSearch(@Nullable @RequestParam String categoryId,
                                       @Nullable @RequestParam Double lowerPrice,
                                       @Nullable @RequestParam Double higherPrice,
                                       @Nullable @RequestParam String locationTitle,
                                       @Nullable @RequestParam String description,
                                       Pageable pageable){

        return ResponseEntity.ok(findByCriteria(categoryId, lowerPrice, higherPrice, locationTitle, description, pageable));
    }

}
