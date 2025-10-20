package com.example.demo.blogs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    private final BlogRepository repo;

    public BlogController(BlogRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Blog> findAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> findOne(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Blog> create(@RequestBody Blog in) {
        return ResponseEntity.ok(repo.save(in));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Blog> update(@PathVariable Long id, @RequestBody Blog in) {
        return repo.findById(id).map(b -> {
            b.setTitle(in.getTitle());
            b.setContent(in.getContent());
            b.setAuthor(in.getAuthor());
            return ResponseEntity.ok(repo.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

