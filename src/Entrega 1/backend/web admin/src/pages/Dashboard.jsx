import { Outlet, Link, useLocation } from 'react-router-dom';
import { Users, Activity, LogOut, Dumbbell, ClipboardList, FileCheck, Bell } from 'lucide-react'; // Bell adicionado

export default function Dashboard() {
  const location = useLocation();

  const baseStyle = {
    display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '8px', transition: 'all 0.2s', textDecoration: 'none'
  };

  const activeStyle = {
    ...baseStyle,
    color: '#FFF',
    background: 'rgba(255,255,255,0.1)'
  };

  const inactiveStyle = {
    ...baseStyle,
    color: '#94A3B8',
    background: 'transparent'
  };

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      
      {/* Sidebar Lateral */}
      <aside style={{ width: '260px', background: 'var(--bg-sidebar)', color: 'white', padding: '32px 24px', display: 'flex', flexDirection: 'column' }}>
        <h2 style={{ color: 'var(--primary)', marginBottom: '48px', fontSize: '24px', display:'flex', alignItems:'center', gap:'12px' }}>
          <Activity size={28} /> Maya Admin
        </h2>
        
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <p style={{ fontSize: '12px', color: '#64748b', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '8px', fontWeight: 'bold' }}>Menu Principal</p>
          
          <Link to="/dashboard" style={location.pathname === '/dashboard' ? activeStyle : inactiveStyle}>
            <Activity size={20} /> Visão Geral
          </Link>
          <Link to="/dashboard/pacientes" style={location.pathname.startsWith('/dashboard/pacientes') ? activeStyle : inactiveStyle}>
            <Users size={20} /> Gestão de Pacientes
          </Link>
          <Link to="/dashboard/exercicios" style={location.pathname.startsWith('/dashboard/exercicios') ? activeStyle : inactiveStyle}>
            <Dumbbell size={20} /> Banco de Exercícios
          </Link>
          <Link to="/dashboard/prontuarios" style={location.pathname.startsWith('/dashboard/prontuarios') ? activeStyle : inactiveStyle}>
            <ClipboardList size={20} /> Prontuários
          </Link>
          <Link to="/dashboard/prescricoes" style={location.pathname.startsWith('/dashboard/prescricoes') ? activeStyle : inactiveStyle}>
            <FileCheck size={20} /> Prescrições
          </Link>

          {/* NOVO LINK: Central de Notificações */}
          <Link to="/dashboard/notificacoes" style={location.pathname.startsWith('/dashboard/notificacoes') ? activeStyle : inactiveStyle}>
            <Bell size={20} /> Notificações
          </Link>
          
        </nav>
        
        <div style={{ flexGrow: 1 }} />
        
        <Link to="/login" style={{ display: 'flex', alignItems: 'center', gap: '12px', color: 'var(--accent)', marginTop: 'auto', padding: '12px', textDecoration: 'none' }}>
          <LogOut size={20} /> Sair da Conta
        </Link>
      </aside>

      {/* Conteúdo Principal Dinâmico */}
      <main style={{ flex: 1, padding: '48px', overflowY: 'auto', background: '#F8FAFC' }}>
        <Outlet />
      </main>
    </div>
  );
}