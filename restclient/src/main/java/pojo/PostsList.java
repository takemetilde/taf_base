package pojo;

import java.util.ArrayList;
import java.util.List;

public class PostsList {

    List<Posts> postsList = new ArrayList<Posts>();

    public PostsList() {
    }

    public PostsList(List<Posts> postsList) {
        this.postsList = postsList;
    }

    public List<Posts> getPostsList() {
        return postsList;
    }

    @Override
    public String toString() {
        return "PostsList{" +
                "postsList=" + postsList +
                '}';
    }
}
