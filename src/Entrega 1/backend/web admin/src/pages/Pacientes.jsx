import { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import { Search, Plus, Edit2, Trash2, CheckCircle, XCircle } from 'lucide-react'; 
import bcrypt from 'bcryptjs';

export default function Pacientes() {
  const [pacientes, setPacientes] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentId, setCurrentId] = useState(null);
  const [formNome, setFormNome] = useState('');
  const [formEmail, setFormEmail] = useState('');
  const [formSenha, setFormSenha] = useState('');
  const [formStatus, setFormStatus] = useState('Ativo'); 

  useEffect(() => {
    fetchPacientes();
  }, []);

  const fetchPacientes = async () => {
    setLoading(true);
    const { data, error } = await supabase.from('pacientes').select('*').order('id', { ascending: false });
    if (!error && data) {
      setPacientes(data);
    }
    setLoading(false);
  };

  const handleSavePaciente = async (e) => {
    e.preventDefault();

    const nomeLimpo = formNome.trim();
    if (nomeLimpo.split(' ').length < 2) {
      alert("⚠️ Atenção: Por favor, insira o nome e o sobrenome do paciente.");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formEmail)) {
      alert("⚠️ Atenção: Por favor, insira um e-mail válido (exemplo: paciente@email.com).");
      return;
    }

    // FUNÇÃO PARA GERAR O HASH ESPECÍFICO $2a$12$
    const gerarHash2a12 = (senha) => {
        const salt = bcrypt.genSaltSync(12);
        const hash = bcrypt.hashSync(senha, salt);
        // Força a substituição de $2b$ por $2a$ caso a biblioteca gere $2b$
        return hash.replace(/^\$2b\$/, '$2a$');
    };

    if (isEditing) {
      let dadosParaAtualizar = { 
        nome: nomeLimpo, 
        email: formEmail,
        status: formStatus 
      };

      if (formSenha.trim() !== '') {
        dadosParaAtualizar.senha = gerarHash2a12(formSenha);
      }

      const { error } = await supabase.from('pacientes').update(dadosParaAtualizar).eq('id', currentId);

      if (!error) {
        closeModal();
        fetchPacientes();
      } else {
        alert("Erro ao editar paciente: " + error.message);
      }

    } else {
      if (!formSenha) return alert("Por favor, defina uma senha para o novo usuário.");

      const senhaSegura = gerarHash2a12(formSenha);

      const { error } = await supabase.from('pacientes').insert([{ 
        nome: nomeLimpo, 
        email: formEmail, 
        senha: senhaSegura,
        status: formStatus 
      }]);

      if (!error) {
        closeModal();
        fetchPacientes();
      } else {
        alert("Erro ao adicionar paciente: " + error.message);
      }
    }
  };

  const handleDeletePaciente = async (id, nome) => {
    const confirmDelete = window.confirm(`ATENÇÃO: Tem certeza que deseja DELETAR PERMANENTEMENTE o usuário "${nome}"?`);
    if (confirmDelete) {
      const { error } = await supabase.from('pacientes').delete().eq('id', id);
      if (!error) {
        fetchPacientes(); 
      } else {
        alert("Erro ao deletar paciente: " + error.message);
      }
    }
  };

  const openAddModal = () => {
    setIsEditing(false);
    setCurrentId(null);
    setFormNome('');
    setFormEmail('');
    setFormSenha(''); 
    setFormStatus('Ativo'); 
    setShowModal(true);
  };

  const openEditModal = (paciente) => {
    setIsEditing(true);
    setCurrentId(paciente.id);
    setFormNome(paciente.nome);
    setFormEmail(paciente.email);
    setFormStatus(paciente.status || 'Ativo'); 
    setFormSenha(''); 
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
  };

  const filteredPacientes = pacientes.filter(p => 
    p.nome?.toLowerCase().includes(search.toLowerCase()) || 
    p.email?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <header style={{ marginBottom: '40px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '28px', marginBottom: '8px', color: '#0F172A' }}>Gestão de Usuários</h1>
          <p style={{ color: '#64748B' }}>Visualizar, Adicionar, Editar e Deletar Usuários (CRUD Completo e Seguro).</p>
        </div>
        
        <button 
          onClick={openAddModal}
          style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '8px', 
            background: '#06B6D4', 
            color: '#FFFFFF', 
            border: 'none', 
            padding: '12px 24px', 
            borderRadius: '8px', 
            fontSize: '14px',
            fontWeight: '600', 
            cursor: 'pointer',
            boxShadow: '0 4px 12px rgba(6, 182, 212, 0.3)', 
            transition: 'all 0.2s'
          }}
          onMouseOver={(e) => e.currentTarget.style.background = '#0891B2'} 
          onMouseOut={(e) => e.currentTarget.style.background = '#06B6D4'}
        >
          <Plus size={20} /> Adicionar Novo Usuário
        </button>
      </header>

      <div className="glass-card" style={{ padding: '24px', marginBottom: '24px', display: 'flex', gap: '16px', alignItems: 'center' }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <Search size={20} style={{ position: 'absolute', left: '16px', top: '14px', color: 'var(--text-muted)' }} />
          <input 
            type="text" 
            placeholder="Pesquisar usuários por nome ou e-mail..." 
            className="input-field" 
            style={{ paddingLeft: '48px' }}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div style={{ color: 'var(--text-muted)', fontSize: '14px', fontWeight: '500' }}>
          Total: {filteredPacientes.length} usuário(s)
        </div>
      </div>

      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ background: 'rgba(0,0,0,0.02)', borderBottom: '1px solid var(--border-light)' }}>
              <th style={{ padding: '16px 24px', color: 'var(--text-muted)', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase' }}>Nome do Usuário</th>
              <th style={{ padding: '16px 24px', color: 'var(--text-muted)', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase' }}>E-mail de Acesso</th>
              <th style={{ padding: '16px 24px', color: 'var(--text-muted)', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase' }}>Status</th>
              <th style={{ padding: '16px 24px', color: 'var(--text-muted)', fontWeight: '600', fontSize: '12px', textTransform: 'uppercase', textAlign: 'right' }}>Ações</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="4" style={{ padding: '24px', textAlign: 'center' }}>Carregando dados da nuvem...</td></tr>
            ) : filteredPacientes.length === 0 ? (
               <tr><td colSpan="4" style={{ padding: '24px', textAlign: 'center' }}>Nenhum usuário encontrado no sistema.</td></tr>
            ) : (
              filteredPacientes.map((p) => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--border-light)' }}>
                  <td style={{ padding: '16px 24px', fontWeight: '500', opacity: p.status === 'Inativo' ? 0.6 : 1 }}>{p.nome}</td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-muted)', opacity: p.status === 'Inativo' ? 0.6 : 1 }}>{p.email}</td>
                  <td style={{ padding: '16px 24px' }}>
                    {p.status === 'Ativo' ? (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 12px', background: '#D1FAE5', color: '#065F46', border: '1px solid #10B981', borderRadius: '24px', fontSize: '12px', fontWeight: '600' }}>
                        <CheckCircle size={14} /> Ativo
                      </span>
                    ) : (
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '6px 12px', background: '#F1F5F9', color: '#64748B', border: '1px solid #CBD5E1', borderRadius: '24px', fontSize: '12px', fontWeight: '600' }}>
                        <XCircle size={14} /> Inativo
                      </span>
                    )}
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '16px' }}>
                      <button 
                        style={{ background: 'transparent', border:'none', color: '#3B82F6', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: '600' }}
                        onClick={() => openEditModal(p)}
                      >
                        <Edit2 size={16} /> Editar
                      </button>
                      <button 
                        style={{ background: 'transparent', border:'none', color: '#EF4444', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: '600' }}
                        onClick={() => handleDeletePaciente(p.id, p.nome)}
                      >
                        <Trash2 size={16} /> Deletar
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50 }}>
          <div className="glass-card" style={{ padding: '32px', width: '100%', maxWidth: '500px', background: '#FFF' }}>
            <h2 style={{ marginBottom: '24px' }}>{isEditing ? 'Editar Usuário Existente' : 'Adicionar Novo Usuário'}</h2>
            <form onSubmit={handleSavePaciente} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label className="label-text">Nome do Usuário</label>
                <input required type="text" className="input-field" placeholder="Ex: Maria Silva" value={formNome} onChange={e => setFormNome(e.target.value)} />
              </div>
              <div>
                <label className="label-text">E-mail (Para Acesso no App)</label>
                <input required type="email" className="input-field" placeholder="exemplo@email.com" value={formEmail} onChange={e => setFormEmail(e.target.value)} />
              </div>
              
              <div>
                <label className="label-text">Status do Paciente no Sistema</label>
                <select className="input-field" value={formStatus} onChange={e => setFormStatus(e.target.value)}>
                  <option value="Ativo">🟢 Ativo (Em Tratamento)</option>
                  <option value="Inativo">⚪ Inativo (Alta ou Pausa)</option>
                </select>
              </div>

              <div>
                <label className="label-text">Senha de Acesso</label>
                <input 
                  required={!isEditing} 
                  type="password" 
                  className="input-field" 
                  placeholder={isEditing ? "Deixe em branco para manter a atual" : "Crie uma senha forte"} 
                  value={formSenha} 
                  onChange={e => setFormSenha(e.target.value)} 
                />
                {isEditing && <small style={{ color: '#64748B', display: 'block', marginTop: '4px' }}>Apenas digite algo aqui se quiser redefinir a senha do paciente.</small>}
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
                <button 
                  type="button" 
                  style={{ background: '#F1F5F9', color: '#64748B', border: 'none', padding: '10px 20px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s' }} 
                  onMouseOver={(e) => e.currentTarget.style.background = '#E2E8F0'} 
                  onMouseOut={(e) => e.currentTarget.style.background = '#F1F5F9'}
                  onClick={closeModal}
                >
                  Cancelar
                </button>

                <button 
                  type="submit" 
                  style={{ display: 'flex', alignItems: 'center', gap: '8px', background: '#06B6D4', color: '#FFFFFF', border: 'none', padding: '10px 24px', borderRadius: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer', boxShadow: '0 4px 12px rgba(6, 182, 212, 0.3)', transition: 'all 0.2s' }}
                  onMouseOver={(e) => e.currentTarget.style.background = '#0891B2'} 
                  onMouseOut={(e) => e.currentTarget.style.background = '#06B6D4'}
                >
                  {isEditing ? 'Salvar Edição' : 'Adicionar Usuário'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}