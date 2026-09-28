import './App.css'
import TasksPage from './pages/TasksPage.jsx'

function App() {
  return (
    <div className="app-container">
      <header className="app-header">
        <h1 className="app-title">TaskNest</h1>
        <p className="app-tagline">Personal Task Management</p>
      </header>
      <main className="app-main">
        <TasksPage />
      </main>
      <footer className="app-footer">
        <p>&copy; {new Date().getFullYear()} TaskNest</p>
      </footer>
    </div>
  )
}

export default App

