import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import HomeDashboard from './pages/HomeDashboard';
import Pacientes from './pages/Pacientes';
import Exercicios from './pages/Exercicios';
import Prontuarios from './pages/Prontuarios';
import Prescricoes from './pages/Prescricoes';
import Notificacoes from './pages/Notificacoes';
import CadastroProfissional from './pages/CadastroProfissional';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />}>
          <Route index element={<HomeDashboard />} />
          <Route path="pacientes" element={<Pacientes />} />
          <Route path="exercicios" element={<Exercicios />} />
          <Route path="prontuarios" element={<Prontuarios />} />
          <Route path="prescricoes" element={<Prescricoes />} />
          <Route path="notificacoes" element={<Notificacoes />} />
          <Route path="configuracoes/usuarios" element={<CadastroProfissional />} />          
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;