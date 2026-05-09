document.addEventListener('DOMContentLoaded', () => {
  // State Management
  const AppState = {
    todos: JSON.parse(localStorage.getItem('glass-todos')) || [],
    filter: 'all',
    
    save() {
      localStorage.setItem('glass-todos', JSON.stringify(this.todos));
    },
    
    add(todo) {
      this.todos.unshift({
        id: Date.now().toString(36) + Math.random().toString(36).substr(2),
        text: todo.text,
        priority: todo.priority,
        completed: false,
        createdAt: Date.now()
      });
      this.save();
      this.notify();
    },
    
    toggle(id) {
      const todo = this.todos.find(t => t.id === id);
      if (todo) {
        todo.completed = !todo.completed;
        this.save();
        this.notify();
      }
    },
    
    delete(id) {
      this.todos = this.todos.filter(t => t.id !== id);
      this.save();
      this.notify();
    },
    
    edit(id, newText) {
      const todo = this.todos.find(t => t.id === id);
      if (todo && newText.trim()) {
        todo.text = newText.trim();
        this.save();
        this.notify();
      }
    },
    
    clearCompleted() {
      this.todos = this.todos.filter(t => !t.completed);
      this.save();
      this.notify();
    },
    
    setFilter(filter) {
      this.filter = filter;
      this.notify();
    },
    
    getFilteredTodos() {
      switch (this.filter) {
        case 'active':
          return this.todos.filter(t => !t.completed);
        case 'completed':
          return this.todos.filter(t => t.completed);
        default:
          return this.todos;
      }
    },
    
    getStats() {
      const total = this.todos.length;
      const completed = this.todos.filter(t => t.completed).length;
      return {
        total,
        active: total - completed,
        completed
      };
    },
    
    listeners: [],
    
    subscribe(listener) {
      this.listeners.push(listener);
      return () => {
        this.listeners = this.listeners.filter(l => l !== listener);
      };
    },
    
    notify() {
      this.listeners.forEach(listener => listener());
    }
  };
  
  // DOM Elements
  const elements = {
    form: document.getElementById('todo-form'),
    input: document.getElementById('todo-input'),
    prioritySelect: document.getElementById('priority-select'),
    list: document.getElementById('todo-list'),
    emptyState: document.getElementById('empty-state'),
    filterBtns: document.querySelectorAll('.filter-btn'),
    clearBtn: document.getElementById('clear-completed-btn'),
    stats: {
      total: document.getElementById('stat-total-count'),
      active: document.getElementById('stat-active-count'),
      completed: document.getElementById('stat-completed-count')
    },
    dateDisplay: document.getElementById('date-display'),
    template: document.getElementById('todo-item-template')
  };
  
  // Initialize Date
  function updateDate() {
    const options = { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric', 
      weekday: 'long' 
    };
    elements.dateDisplay.textContent = new Date().toLocaleDateString('zh-CN', options);
  }
  
  // Create Todo Element
  function createTodoElement(todo) {
    const clone = elements.template.content.cloneNode(true);
    const li = clone.querySelector('.todo-item');
    const checkbox = clone.querySelector('.todo-checkbox');
    const text = clone.querySelector('.todo-text');
    const badge = clone.querySelector('.todo-priority-badge');
    const editBtn = clone.querySelector('.edit-btn');
    const deleteBtn = clone.querySelector('.delete-btn');
    
    li.dataset.id = todo.id;
    if (todo.completed) {
      li.classList.add('completed');
      checkbox.checked = true;
    }
    
    text.textContent = todo.text;
    badge.textContent = getPriorityLabel(todo.priority);
    badge.dataset.priority = todo.priority;
    
    checkbox.addEventListener('change', () => AppState.toggle(todo.id));
    
    editBtn.addEventListener('click', () => {
      const newText = prompt('编辑任务:', todo.text);
      if (newText !== null) {
        AppState.edit(todo.id, newText);
      }
    });
    
    deleteBtn.addEventListener('click', () => {
      li.style.animation = 'slideOut 0.3s ease forwards';
      setTimeout(() => AppState.delete(todo.id), 300);
    });
    
    return li;
  }
  
  function getPriorityLabel(priority) {
    const labels = {
      high: '高',
      medium: '中',
      low: '低'
    };
    return labels[priority] || priority;
  }
  
  // Render
  function render() {
    const todos = AppState.getFilteredTodos();
    const stats = AppState.getStats();
    
    // Update stats
    elements.stats.total.textContent = stats.total;
    elements.stats.active.textContent = stats.active;
    elements.stats.completed.textContent = stats.completed;
    
    // Update list
    elements.list.innerHTML = '';
    
    if (todos.length === 0) {
      elements.emptyState.classList.remove('hidden');
      elements.list.classList.add('hidden');
    } else {
      elements.emptyState.classList.add('hidden');
      elements.list.classList.remove('hidden');
      
      todos.forEach(todo => {
        elements.list.appendChild(createTodoElement(todo));
      });
    }
    
    // Update clear button
    if (stats.completed > 0) {
      elements.clearBtn.classList.remove('hidden');
    } else {
      elements.clearBtn.classList.add('hidden');
    }
    
    // Update filter buttons
    elements.filterBtns.forEach(btn => {
      if (btn.dataset.filter === AppState.filter) {
        btn.classList.add('active');
        btn.setAttribute('aria-selected', 'true');
      } else {
        btn.classList.remove('active');
        btn.setAttribute('aria-selected', 'false');
      }
    });
  }
  
  // Event Listeners
  elements.form.addEventListener('submit', (e) => {
    e.preventDefault();
    const text = elements.input.value.trim();
    if (!text) return;
    
    AppState.add({
      text: text,
      priority: elements.prioritySelect.value
    });
    
    elements.input.value = '';
    elements.input.focus();
  });
  
  elements.filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      AppState.setFilter(btn.dataset.filter);
    });
  });
  
  elements.clearBtn.addEventListener('click', () => {
    if (confirm('确定要清除所有已完成的任务吗？')) {
      AppState.clearCompleted();
    }
  });
  
  // Keyboard shortcuts
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      elements.input.blur();
    }
    if ((e.metaKey || e.ctrlKey) && e.key === 'Enter') {
      elements.input.focus();
    }
  });
  
  // Subscribe to state changes
  AppState.subscribe(render);
  
  // Initialize
  updateDate();
  render();
  elements.input.focus();
  
  // Add slideOut animation dynamically
  const style = document.createElement('style');
  style.textContent = `
    @keyframes slideOut {
      to {
        opacity: 0;
        transform: translateX(100px);
        height: 0;
        margin: 0;
        padding: 0;
      }
    }
  `;
  document.head.appendChild(style);
});