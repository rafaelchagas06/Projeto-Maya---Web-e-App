import { useState } from 'react';
import { supabase } from '../supabaseClient';
import { UserPlus, Mail, Lock, User, ShieldCheck } from 'lucide-react';
import bcrypt from 'bcryptjs';

export default function CadastroProfissional() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [loading, setLoading] = useState(false);

  const handleCadastro = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // 1. GERAÇÃO DO HASH PADRÃO $2a$12$ (Requisito de Segurança E2)
      const salt = bcrypt.genSaltSync(12);
      let hash = bcrypt.hashSync(senha, salt);
      hash = hash.replace(/^\$2b\$/, '$2a$'); // Força o prefixo solicitado

      // 2. INSERÇÃO NA TABELA DE PROFISSIONAIS
      const { error } = await supabase
        .from('profissionais')
        .insert([{ nome, email, senha: hash }]);

      if (error) throw error;

      alert("Profissional cadastrado com sucesso! Agora ele pode acessar o Portal Web.");
      setNome('');
      setEmail('');
      setSenha('');
    } catch (err) {
      alert("Erro ao cadastrar: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <header style={{ marginBottom: '40px' }}>
        <h1 style={{ fontSize: '28px', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '12px' }}>
          <ShieldCheck size={32} color="var(--primary)" /> Novo Acesso Administrativo
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>Crie credenciais para novos fisioterapeutas ou administradores do sistema.</p>
      </header>

      <div className="glass-card" style={{ padding: '32px' }}>
        <form onSubmit={handleCadastro} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label className="label-text">Nome Completo</label>
            <div style={{ position: 'relative' }}>
              <User size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
              <input required type="text" className="input-field" style={{ paddingLeft: '40px' }} placeholder="Ex: Dr. Ricardo Silva" value={nome} onChange={e => setNome(e.target.value)} />
            </div>
          </div>

          <div>
            <label className="label-text">E-mail Corporativo</label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
              <input required type="email" className="input-field" style={{ paddingLeft: '40px' }} placeholder="clinica@maya.com" value={email} onChange={e => setEmail(e.target.value)} />
            </div>
          </div>

          <div>
            <label className="label-text">Senha de Acesso</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
              <input required type="password" className="input-field" style={{ paddingLeft: '40px' }} placeholder="••••••••" value={senha} onChange={e => setSenha(e.target.value)} />
            </div>
          </div>

          <button type="submit" disabled={loading} className="btn-primary" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', padding: '14px', marginTop: '10px' }}>
            <UserPlus size={20} /> {loading ? 'Cadastrando...' : 'Criar Conta Profissional'}
          </button>
        </form>
      </div>
    </div>
  );
}