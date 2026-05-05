import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Stethoscope } from 'lucide-react';
import { supabase } from '../supabaseClient';
import bcrypt from 'bcryptjs'; // Importação da biblioteca de criptografia

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false); // Novo estado para o botão carregando
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // 1. CHAVE MESTRA: Acesso rápido do Fisioterapeuta (Mantido para a apresentação)
    if (email === 'admin@maya.com' && password === 'admin') {
      localStorage.setItem('maya_usuario_logado', JSON.stringify({ tipo: 'admin', nome: 'Fisioterapeuta' }));
      navigate('/dashboard');
      setLoading(false);
      return;
    }

    // 2. ACESSO DE USUÁRIOS/PACIENTES: Validação Real no Banco com Criptografia (LGPD)
    try {
      const { data: usuario, error: dbError } = await supabase
        .from('pacientes')
        .select('*')
        .eq('email', email.trim())
        .single();

      if (dbError || !usuario) {
        setError('Acesso negado. E-mail não encontrado no sistema.');
        setLoading(false);
        return;
      }

      // Proteção de segurança: impede o login se a senha no banco não estiver criptografada
      if (!usuario.senha || (!usuario.senha.startsWith('$2a$') && !usuario.senha.startsWith('$2b$'))) {
        setError('Por segurança, redefina a senha deste usuário no painel de Gestão.');
        setLoading(false);
        return;
      }

      // Verifica se a senha digitada bate com a criptografia salva no banco
      const senhaCorreta = bcrypt.compareSync(password, usuario.senha);

      if (senhaCorreta) {
        // Salva os dados na sessão e libera o acesso
        localStorage.setItem('maya_usuario_logado', JSON.stringify({ id: usuario.id, nome: usuario.nome, tipo: 'paciente' }));
        navigate('/dashboard');
      } else {
        setError('Senha incorreta. Verifique suas credenciais.');
      }
    } catch (err) {
      setError('Erro de comunicação com o servidor de autenticação.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundImage: 'linear-gradient(135deg, #F5F7FA 0%, #E2E8F0 100%)' }}>
      <div className="glass-card" style={{ padding: '48px 40px', width: '100%', maxWidth: '420px' }}>
        <h2 style={{ textAlign: 'center', color: 'var(--primary-dark)', marginBottom: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', fontSize: '28px' }}>
          <Stethoscope size={32} /> Clínica Maya
        </h2>
        <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: '32px', fontWeight: '500' }}>Portal do Fisioterapeuta</p>
        
        {/* Mensagem de Erro */}
        {error && (
          <div style={{ color: 'var(--accent)', background: 'rgba(240, 113, 103, 0.1)', padding: '12px', borderRadius: '8px', marginBottom: '24px', fontSize: '14px', textAlign: 'center', border: '1px solid rgba(240, 113, 103, 0.3)' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label className="label-text">E-mail Corporativo ou Paciente</label>
            <input 
              type="email" 
              className="input-field" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              placeholder="admin@maya.com" 
              required 
            />
          </div>
          <div>
            <label className="label-text">Senha</label>
            <input 
              type="password" 
              className="input-field" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              placeholder="••••••••"
              required 
            />
          </div>
          <button 
            type="submit" 
            className="btn-primary" 
            disabled={loading}
            style={{ 
              marginTop: '8px', 
              padding: '14px', 
              fontSize: '16px', 
              opacity: loading ? 0.7 : 1, 
              cursor: loading ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s'
            }}
          >
            {loading ? 'VERIFICANDO...' : 'ACESSAR PAINEL'}
          </button>
        </form>
      </div>
    </div>
  );
}