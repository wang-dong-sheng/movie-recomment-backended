package pqdong.movie.recommend.service;/**
 * @author Mr.Wang
 * @create 2025-04-16-15:47
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pqdong.movie.recommend.constant.ServerConstant;
import pqdong.movie.recommend.constant.Constant;
import pqdong.movie.recommend.data.entity.PersonTemp;
import pqdong.movie.recommend.utils.RecommendUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *@ClassName PersonMongoService
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/4/16 15:47
 *@Version 1.0
 */
@Service
public class PersonNewService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> personCollection;

    private MongoCollection<Document> getPersonCollection() {
        if (null == personCollection)
            personCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection("Person");
        return personCollection;
    }

    private PersonTemp documentToPerson(Document document) {
        PersonTemp person = null;
        try {
            person = objectMapper.readValue(JSON.serialize(document), PersonTemp.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return person;
    }

    public Map<String, Object> getAllPerson(String key, int page, int size) {
        Pair<Integer, Integer> pair = RecommendUtils.getStartAndEnd(page, size);
        List<PersonTemp> allPerson = getPersons(key, page * size);
        List<PersonTemp> personList = allPerson.subList(pair.getLeft(), pair.getRight() <= allPerson.size() ? pair.getRight() : allPerson.size());
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put("total", personList.size());
        result.put("personList", personList.stream().peek(p -> {
            if (StringUtils.isEmpty(p.getAvatar())) {
                p.setAvatar(ServerConstant.DefaultImg);
            }
        }).collect(Collectors.toCollection(LinkedList::new)));
        return result;
    }

    private List<PersonTemp> getPersons(String key, int total) {
        List<PersonTemp> personList = new LinkedList<>();
        FindIterable<Document> documents;
        
        if (StringUtils.isBlank(key)) {
            documents = getPersonCollection().find().limit(total);
        } else {
            documents = getPersonCollection().find(Filters.regex("name", ".*" + key + ".*"));
        }
        
        for (Document document : documents) {
            personList.add(documentToPerson(document));
        }
        return personList;
    }

    public PersonTemp getPersonById(Integer personId) {
        Document document = getPersonCollection().find(new Document("personId", personId)).first();
        if (document == null || document.isEmpty()) {
            return null;
        }
        return documentToPerson(document);
    }
}


